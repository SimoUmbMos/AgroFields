package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private final RoomDatabase db;

    public UserRepositoryImpl(RoomDatabase db) {
        this.db = db;
    }

    @Override
    public User getUserByID(long id){
        User u = db.userDao().getUserById(id);
        if(u != null){
            return EncryptUtil.decrypt(u);
        }
        return null;
    }
    @Override
    public User getUserByUserName(String username) {
        return db.userDao().getUserByUserName(username);
    }
    @Override
    public User getUserByUserNameAndPassword(String username, String password) {
        String encryptedPassword = EncryptUtil.encryptPassword(password);
        return db.userDao().getUserByUserNameAndPassword(username, encryptedPassword);
    }

    @Override
    public List<User> userSearch(User searchUser, String search) {
        if(search.length() > 3 && searchUser != null){
            List<User> result = db.userDao().searchUserByUserName(
                    searchUser.getId(),
                    search
            );
            removeUsers(searchUser, result, UserDBAction.BLOCKED);
            removeUsers(searchUser, result, UserDBAction.REQUESTED);
            removeUsers(searchUser, result, UserDBAction.FRIENDS);
            return result;
        }
        return null;
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
    public void removeBlock(User currUser, User otherUser){
        db.userRelationshipDao().deleteByIDsAndType(
                currUser.getId(),
                otherUser.getId(),
                UserDBAction.BLOCKED
        );
    }

    @Override
    public List<User> getUserFriendList(User user) {
        List<User> friends = db.userDao().getUsersByReceiverIDAndType(
                user.getId(),
                UserDBAction.FRIENDS
        );
        friends.addAll(db.userDao().getUsersBySenderIDAndType(
                user.getId(),
                UserDBAction.FRIENDS
        ));
        return EncryptUtil.decryptAll(friends);
    }
    @Override
    public List<User> getUserFriendRequestList(User user) {
        if(user != null){
            return db.userDao().getUsersByReceiverIDAndType(
                    user.getId(),
                    UserDBAction.REQUESTED
            );
        }
        return new ArrayList<>();
    }
    @Override
    public List<User> getUserBlockList(User user) {
        if(user != null){
            return db.userDao().getUsersBySenderIDAndType(
                    user.getId(),
                    UserDBAction.BLOCKED
            );
        }
        return new ArrayList<>();
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
            db.sharedLandDao().deleteByUserID(user.getId());
            db.landDao().deleteByUID(user.getId());
            db.userRelationshipDao().deleteByUserID(user.getId());
            db.userDao().delete(user);
        }
    }

    private void removeUsers(User searchUser, List<User> result, UserDBAction type) {
        if(result.size() > 0){
            List<UserRelationship> relationships = db.userRelationshipDao().getByIDAndType(
                    searchUser.getId(),
                    type
            );
            List<User> removeList = new ArrayList<>();
            for(UserRelationship relationship : relationships){
                if(relationship.getReceiverID() == searchUser.getId()){
                    removeList.add(db.userDao().getUserById(relationship.getSenderID()));
                }else{
                    removeList.add(db.userDao().getUserById(relationship.getReceiverID()));
                }
            }
            result.removeAll(removeList);
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
