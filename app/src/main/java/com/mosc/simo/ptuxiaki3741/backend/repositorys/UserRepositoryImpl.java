package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private final AppDatabase db;

    public UserRepositoryImpl(AppDatabase db) {
        this.db = db;
    }

    @Override
    public User searchUserByID(long id){
        return db.userDao().getUserById(id);
    }
    @Override
    public List<User> searchUserByKey(String key){
        return db.userDao().getUsersByKey(key);
    }
    @Override
    public List<User> searchUserByUserName(String username){
        return db.userDao().getUsersByUserName(username);
    }
    @Override
    public List<User> getUsersByKeyAndUserName(String key,String username){
        List<User> users = db.userDao().getAccurateUsersByKeyAndUserName(key,username);
        if(users.size() == 0){
            users = db.userDao().getUsersByKeyAndUserName(key,username);
        }
        return users;
    }

    @Override
    public List<User> getUsers(){
        return db.userDao().getUsers();
    }
    @Override
    public User saveUser(User user){
        long id = db.userDao().insert(user);
        user.setId(id);
        return user;
    }
    @Override
    public void deleteUser(User user){
        db.userDao().delete(user);
    }
}
