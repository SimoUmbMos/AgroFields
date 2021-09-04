package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private static final int PAGE_SIZE = 50;
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
    public int searchPageCount(User searchUser, String search) {
        if(search.length() > 3 && searchUser != null){
            int n = db.userDao().searchResultCount(searchUser.getId(),search,UserDBAction.BLOCKED);
            if(n > 0)
                return (int) Math.ceil(n / (PAGE_SIZE * 1.0));
            else
                return 0;
        }
        return -1;
    }
    @Override
    public List<User> userSearch(User searchUser, String search, int p) {
        if(search.length() > 3 && searchUser != null){
            int page;
            if(p > 0)
                page = p * PAGE_SIZE;
            else
                page = 0;
            return db.userDao().searchUserByUserName(
                    searchUser.getId(),
                    search,
                    PAGE_SIZE,
                    page,
                    UserDBAction.BLOCKED
            );
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
    public List<User> getUsers(){
        return db.userDao().getUsers();
    }
    @Override
    public List<User> getUserFriendList(User user) {
        List<UserRelationship> friendRelationships =
                db.userRelationshipDao().getByIDAndType(user.getId(), UserDBAction.FRIENDS);
        List<User> friends = new ArrayList<>();
        User friend;
        for(UserRelationship friendRelationship:friendRelationships){
            if(user.getId() == friendRelationship.getReceiverID()){
                friend = db.userDao().getUserById(friendRelationship.getSenderID());
            }else{
                friend = db.userDao().getUserById(friendRelationship.getReceiverID());
            }
            if(friend != null)
                friends.add(friend);
        }
        return EncryptUtil.decryptAll(friends);
    }
    @Override
    public List<User> getUserFriendRequestList(User user) {
        List<UserRelationship> requestRelationships = db.userRelationshipDao()
                .getByReceiverIDAndType(user.getId(), UserDBAction.REQUESTED);

        List<User> requests = new ArrayList<>();
        User request;
        for(UserRelationship requestRelationship:requestRelationships){
            request = db.userDao().getUserById(requestRelationship.getSenderID());
            if(request != null)
                requests.add(request);
        }

        return requests;
    }
    @Override
    public List<User> getUserBlockList(User user) {
        List<UserRelationship> blockRelationships = db.userRelationshipDao()
                .getBySenderIDAndType(user.getId(), UserDBAction.BLOCKED);

        List<User> blockedUsers = new ArrayList<>();
        User blockedUser;
        for(UserRelationship blockRelationship : blockRelationships){
            blockedUser = db.userDao().getUserById(blockRelationship.getReceiverID());
            if(blockedUser != null)
                blockedUsers.add(blockedUser);
        }

        return blockedUsers;
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
