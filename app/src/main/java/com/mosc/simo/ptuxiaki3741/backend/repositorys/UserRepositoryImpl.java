package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.util.UserUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.util.UserUtil.getAllReceiverUsers;
import static com.mosc.simo.ptuxiaki3741.util.UserUtil.getAllSenderUsers;

public class UserRepositoryImpl implements UserRepository {
    private final RoomDatabase db;

    public UserRepositoryImpl(RoomDatabase db) {
        this.db = db;
    }

    @Override
    public User searchUserByID(long id){
        User u = db.userDao().getUserById(id);
        if(u != null){
            return EncryptUtil.decrypt(u);
        }
        return null;
    }
    @Override
    public User searchUserByUserName(String username) {
        return db.userDao().getUserByUserName(username);
    }
    @Override
    public User searchUserByUserNameAndPassword(String username, String password) {
        String encryptedPassword = EncryptUtil.encryptPassword(password);
        return db.userDao().getUserByUserNameAndPassword(username, encryptedPassword);
    }
    @Override
    public List<User> userSearch(User user, String username) {
        List<User> searchResult = db.userDao().searchUserByUserName(user.getId(),username);
        List<User> tempList = UserUtil.getAllSenderUsers(
                db.userRelationshipDao().getByReceiverIDAndType(user.getId(), UserDBAction.BLOCKED),
                db
        );
        tempList.addAll(UserUtil.getAllReceiverUsers(
                db.userRelationshipDao().getBySenderIDAndType(user.getId(), UserDBAction.BLOCKED),
                db
        ));
        searchResult.removeAll(tempList);
        tempList.clear();

        tempList = UserUtil.getAllSenderUsers(
                db.userRelationshipDao().getByReceiverIDAndType(user.getId(), UserDBAction.FRIENDS),
                db
        );
        tempList.addAll(UserUtil.getAllReceiverUsers(
                db.userRelationshipDao().getBySenderIDAndType(user.getId(), UserDBAction.FRIENDS),
                db
        ));
        List<User> common = new ArrayList<>(searchResult);
        common.retainAll(tempList);
        Collections.reverse(common);
        searchResult.removeAll(common);
        Collections.reverse(searchResult);
        searchResult.addAll(common);
        Collections.reverse(searchResult);
        return searchResult;
    }

    @Override
    public UserFriendRequestStatus sendFriendRequest(User currUser, User receiver){
        if(currUser != null && receiver != null ){
            if(getUserFriendRequestList(currUser).contains(receiver)){
                if(acceptFriendRequest(currUser,receiver)){
                    return UserFriendRequestStatus.ACCEPTED;
                }
            }else{
                List<UserRelationship> relationships = db.userRelationshipDao().getByIDs(
                        currUser.getId(),
                        receiver.getId()
                );
                if(relationships == null){
                    saveUserRelationship(currUser,receiver,UserDBAction.REQUESTED);
                    return UserFriendRequestStatus.REQUESTED;
                }else if(relationships.size() == 0){
                    saveUserRelationship(currUser,receiver,UserDBAction.REQUESTED);
                    return UserFriendRequestStatus.REQUESTED;
                }
            }
        }
        return UserFriendRequestStatus.REQUEST_FAILED;
    }
    @Override
    public boolean acceptFriendRequest(User currUser, User sender){
        if( currUser != null && sender != null ){
            if(getUserFriendRequestList(currUser).contains(sender)){
                deleteUserRelationship(currUser,sender);
                saveUserRelationship(currUser,sender,UserDBAction.FRIENDS);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean declineFriendRequest(User currUser, User sender){
        if( currUser != null && sender != null ){
            List<User> currUserRequests = getUserFriendRequestList(currUser);
            if(currUserRequests.contains(sender)){
                deleteUserRelationship(currUser,sender);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean deleteFriend(User currUser, User friend){
        if(currUser != null && friend!= null){
            if(getUserFriendList(currUser).contains(friend)){
                deleteUserRelationship(currUser,friend);
                return true;
            }
        }
        return false;
    }
    @Override
    public void blockUser(User currUser, User otherUser){
        deleteUserRelationship(currUser,otherUser);
        saveUserRelationship(currUser,otherUser,UserDBAction.BLOCKED);
    }

    @Override
    public List<User> getUsers(){
        return db.userDao().getUsers();
    }
    @Override
    public List<User> getUserFriendList(User user) {
        List<UserRelationship> friendSenderRelationship = db.userRelationshipDao()
                .getBySenderIDAndType(user.getId(), UserDBAction.FRIENDS);
        List<UserRelationship> friendReceiverRelationship = db.userRelationshipDao()
                .getByReceiverIDAndType(user.getId(), UserDBAction.FRIENDS);

        List<User> friends = UserUtil
                .getAllReceiverUsers(friendSenderRelationship, db);
        friends.addAll(UserUtil
                .getAllSenderUsers(friendReceiverRelationship, db));

        return EncryptUtil.decryptAll(friends);
    }
    @Override
    public List<User> getUserFriendRequestList(User user) {
        List<UserRelationship> requestRelationship = db.userRelationshipDao()
                .getByReceiverIDAndType(user.getId(), UserDBAction.REQUESTED);
        return getAllSenderUsers(requestRelationship, db);
    }
    @Override
    public List<User> getUserBlockList(User user) {
        List<UserRelationship> blockRelationship = db.userRelationshipDao()
                .getBySenderIDAndType(user.getId(), UserDBAction.BLOCKED);
        return getAllReceiverUsers(blockRelationship, db);
    }

    @Override
    public User saveNewUser(User newUser){
        if(newUser != null){
            User user = db.userDao().getUserByUserName(newUser.getUsername());
            if(user == null){
                user = EncryptUtil.encryptWithPassword(newUser);
                long id = db.userDao().insert(user);
                user.setId(id);
                return user;
            }
        }
        return null;
    }
    @Override
    public void editUser(User user) {
        if(user != null){
            db.userDao().insert(EncryptUtil.encrypt(user));
        }
    }
    @Override
    public void deleteUser(User user){
        if(user != null){
            db.userRelationshipDao().deleteByUserID(user.getId());
            db.userDao().delete(user);
        }
    }

    private void saveUserRelationship(User sender, User receiver, UserDBAction type) {
        deleteUserRelationship(sender,receiver);
        db.userRelationshipDao().insert(
                new UserRelationship(sender.getId(),receiver.getId(), type, new Date())
        );
    }
    private void deleteUserRelationship(User user1, User user2) {
        List<UserRelationship> relationships =
                db.userRelationshipDao().getByIDs(user1.getId(),user2.getId());
        db.userRelationshipDao().deleteAll(relationships);
    }
}
