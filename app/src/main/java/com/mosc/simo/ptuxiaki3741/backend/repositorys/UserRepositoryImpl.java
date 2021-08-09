package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil.getAllReceiverUsers;
import static com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil.getAllSenderUsers;

public class UserRepositoryImpl implements UserRepository {
    private final RoomDatabase db;

    public UserRepositoryImpl(RoomDatabase db) {
        this.db = db;
    }

    @Override
    public User searchUserByID(long id){
        return db.userDao().getUserById(id);
    }
    @Override
    public User searchUserByUserName(String username){
        return db.userDao().getUserByUserName(username);
    }
    @Override
    public List<User> userSearch(User user, String username) {
        List<User> searchResult = db.userDao().searchUserByUserName(user.getId(),username);
        List<User> templist = UserRelationshipUtil.getAllSenderUsers(
                db.userRelationshipDao().getByReceiverIDAndType(user.getId(), UserDBAction.BLOCKED),
                db
        );
        templist.addAll(UserRelationshipUtil.getAllReceiverUsers(
                db.userRelationshipDao().getBySenderIDAndType(user.getId(), UserDBAction.BLOCKED),
                db
        ));
        searchResult.removeAll(templist);
        templist.clear();

        templist = UserRelationshipUtil.getAllSenderUsers(
                db.userRelationshipDao().getByReceiverIDAndType(user.getId(), UserDBAction.FRIENDS),
                db
        );
        templist.addAll(UserRelationshipUtil.getAllReceiverUsers(
                db.userRelationshipDao().getBySenderIDAndType(user.getId(), UserDBAction.FRIENDS),
                db
        ));
        List<User> common = new ArrayList<>(searchResult);
        common.retainAll(templist);
        Collections.reverse(common);
        searchResult.removeAll(common);
        Collections.reverse(searchResult);
        searchResult.addAll(common);
        Collections.reverse(searchResult);
        return searchResult;
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

        List<User> friends = UserRelationshipUtil
                .getAllReceiverUsers(friendSenderRelationship, db);
        friends.addAll(UserRelationshipUtil
                .getAllSenderUsers(friendReceiverRelationship, db));

        return friends;
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
        User user = db.userDao().getUserByUserName(newUser.getUsername());
        if(user == null){
            long id = db.userDao().insert(newUser);
            newUser.setId(id);
            return newUser;
        }
        return null;
    }
    @Override
    public void saveUserRelationship(UserRelationship userRelationship) {
        long sender = userRelationship.getSenderID();
        long receiver = userRelationship.getReceiverID();
        UserDBAction type = userRelationship.getType();
        deleteUserRelationship(sender,receiver);
        db.userRelationshipDao().insert(
                new UserRelationship(sender,receiver, type, new Date())
        );
    }
    @Override
    public void saveUserRelationship(User sender, User receiver, UserDBAction type) {
        deleteUserRelationship(sender,receiver);
        db.userRelationshipDao().insert(
                new UserRelationship(sender.getId(),receiver.getId(), type, new Date())
        );
    }

    @Override
    public void editUser(User user) {
        db.userDao().insert(user);
    }

    @Override
    public void deleteUser(User user){
        db.userRelationshipDao().deleteByUserID(user.getId());
        db.userDao().delete(user);
    }
    @Override
    public void deleteUserRelationship(User user1, User user2) {
        List<UserRelationship> relationships =
                db.userRelationshipDao().getByIDs(user1.getId(),user2.getId());
        for (UserRelationship relationship : relationships) {
            db.userRelationshipDao().delete(relationship);
        }
    }
    @Override
    public void deleteUserRelationship(long userID1, long userID2) {
        List<UserRelationship> relationships =
                db.userRelationshipDao().getByIDs(userID1,userID2);
        for (UserRelationship relationship : relationships) {
            db.userRelationshipDao().delete(relationship);
        }
    }
}
