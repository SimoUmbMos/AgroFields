package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import android.util.Log;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;
import com.mosc.simo.ptuxiaki3741.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandMemo;
import com.mosc.simo.ptuxiaki3741.models.entities.UserMemo;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    public static final String TAG = "UserRepositoryImpl";
    private final RoomDatabase db;

    public UserRepositoryImpl(RoomDatabase db) {
        this.db = db;
    }

    @Override
    public User getUserByID(long id){
        User u = db.userDao().getUserById(id);
        User result = null;
        if(u != null){
            try{
                result = EncryptUtil.decrypt(u);
            }catch (Exception e){
                Log.e(TAG, "getUserByID: ", e);
            }
        }
        return result;
    }
    @Override
    public User getUserByUserName(String username) {
        return db.userDao().getUserByUserName(username);
    }
    @Override
    public User getUserByUserNameAndPassword(String username, String password) {
        String encryptedPassword = null;
        try {
            encryptedPassword = EncryptUtil.encryptPassword(password);
        }catch (Exception e){
            Log.e(TAG, "getUserByUserNameAndPassword: ", e);
        }
        User result = null;
        if(encryptedPassword != null){
            result = db.userDao().getUserByUserNameAndPassword(username, encryptedPassword);
        }
        return result;
    }

    @Override
    public List<User> userSearch(User searchUser, String search) {
        if(search.length() > 3 && searchUser != null){
            List<User> result = db.userDao().searchUserByUserName(
                    searchUser.getId(),
                    search
            );
            removeUsers(searchUser, result, UserDBAction.BLOCKED);
            removeUsers(searchUser, result, UserDBAction.FRIENDS);
            return result;
        }
        return null;
    }

    @Override
    public UserFriendRequestStatus sendFriendRequest(User currUser, User receiver){
        if(currUser != null && receiver != null ){
            if(getUserInboxRequestList(currUser).contains(receiver)){
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
    public boolean deleteFriendRequest(User sender, User receiver){
        if(sender != null && receiver != null){
            int res = db.userRelationshipDao().deleteByIDsAndType(
                    receiver.getId(),
                    sender.getId(),
                    UserDBAction.REQUESTED
            );
            return res > 0;
        }
        return false;
    }
    @Override
    public boolean acceptFriendRequest(User currUser, User sender){
        if( currUser != null && sender != null ){
            if(getUserInboxRequestList(currUser).contains(sender)){
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
            List<User> currUserRequests = getUserInboxRequestList(currUser);
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
    public LandMemo saveMemo(User currUser, LandData landData, String memo){
        LandMemo save = new LandMemo(currUser.getId(),landData.getId(),memo);
        long id = db.landMemoDao().insert(save);
        save.setId(id);
        return save;
    }
    @Override
    public UserMemo saveMemo(User currUser, User user, String memo){
        UserMemo save = new UserMemo(currUser.getId(),user.getId(),memo);
        long id = db.userMemoDao().insert(save);
        save.setId(id);
        return save;
    }
    @Override
    public void removeMemo(User currUser, LandData landData){
        db.landMemoDao().deleteByUserAndLand(currUser.getId(),landData.getId());
    }
    @Override
    public void removeMemo(User currUser, User contact){
        db.userMemoDao().deleteByUsers(currUser.getId(),contact.getId());
    }
    @Override
    public void saveMemo(LandMemo memo){
        db.landMemoDao().insert(memo);
    }
    @Override
    public void saveMemo(UserMemo memo){
        db.userMemoDao().insert(memo);
    }
    @Override
    public void removeMemo(LandMemo memo){
        db.landMemoDao().delete(memo);
    }
    @Override
    public void removeMemo(UserMemo memo){
        db.userMemoDao().delete(memo);
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
        List<User> result = new ArrayList<>();
        try {
            result.addAll(EncryptUtil.decryptAll(friends));
        }catch (Exception e){
            Log.e(TAG, "getUserFriendList: ", e);
        }
        return result;
    }
    @Override
    public List<User> getUserInboxRequestList(User user) {
        if(user != null){
            return db.userDao().getUsersByReceiverIDAndType(
                    user.getId(),
                    UserDBAction.REQUESTED
            );
        }
        return new ArrayList<>();
    }
    @Override
    public List<User> getUserOutboxRequestList(User user) {
        if(user != null){
            return db.userDao().getUsersBySenderIDAndType(
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
    public List<UserMemo> getUserFriendsMemosList(User user){
        if(user != null)
            return db.userMemoDao().getUserLandMemo(user.getId());
        return new ArrayList<>();
    }
    @Override
    public List<LandMemo> getUserLandsMemosList(User user){
        if(user != null)
            return db.landMemoDao().getUserLandMemo(user.getId());
        return new ArrayList<>();
    }

    @Override
    public User saveNewUser(User newUser){
        if(newUser != null){
            if(usernameNotExists(newUser)){
                if(emailNotExists(newUser)){
                    User user = null;
                    try{
                        user = EncryptUtil.encryptWithPassword(newUser);
                    }catch (Exception e){
                        Log.e(TAG, "saveNewUser: ", e);
                    }
                    if(user != null){
                        long id = db.userDao().insert(user);
                        user.setId(id);
                    }
                    return user;
                }
            }
        }
        return null;
    }
    @Override
    public LoginRegisterError getNewUserError(User newUser){
        if(newUser != null){
            if(!usernameNotExists(newUser))
                return LoginRegisterError.UserNameTakenError;
            if(!emailNotExists(newUser))
                return LoginRegisterError.EmailTakenError;
        }
        return LoginRegisterError.NONE;
    }

    @Override
    public void editUser(User user) {
        if(user != null){
            User update = null;
            try {
                update = EncryptUtil.encrypt(user);
            }catch (Exception e){
                Log.e(TAG, "editUser: ", e);
            }
            if(update != null)
                db.userDao().insert(update);
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
    private boolean usernameNotExists(User newUser) {
        User user = db.userDao().getUserByUserName(newUser.getUsername());
        return user == null;
    }
    private boolean emailNotExists(User newUser) {
        List<User> users = db.userDao().getAllUsers();
        User temp;
        for(User user:users){
            try{
                temp = EncryptUtil.decrypt(user);
                if(newUser.getEmail().equals(temp.getEmail())){
                    return false;
                }
            }catch (Exception e){
                Log.e(TAG, "emailExists: ", e);
            }
        }
        return true;
    }

}
