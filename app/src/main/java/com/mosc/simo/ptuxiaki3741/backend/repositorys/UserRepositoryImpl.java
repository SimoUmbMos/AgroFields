package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private final RoomDatabase db;

    public UserRepositoryImpl(RoomDatabase db) {
        this.db = db;
    }

    @Override
    public List<User> getUsers(){
        return db.userDao().getUsers();
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
    public User searchUserByID(long id){
        return db.userDao().getUserById(id);
    }
    @Override
    public User searchUserByUserName(String username){
        return db.userDao().getUserByUserName(username);
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
    public void editUser(User user) {
        db.userDao().insert(user);
    }
    @Override
    public void deleteUser(User user){
        db.userDao().delete(user);
    }
}
