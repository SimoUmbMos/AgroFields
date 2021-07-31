package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.models.User;

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
    public User searchUserByID(long id){
        return db.userDao().getUserById(id);
    }
    @Override
    public User searchUserByUserName(String username){
        return db.userDao().getUserByUserName(username);
    }
    @Override
    public boolean saveNewUser(User newUser){
        User user = db.userDao().getUserByUserName(newUser.getUsername());
        if(user == null){
            long id = db.userDao().insert(newUser);
            newUser.setId(id);
            return true;
        }
        return false;
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
