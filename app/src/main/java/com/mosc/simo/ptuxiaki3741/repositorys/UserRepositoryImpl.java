package com.mosc.simo.ptuxiaki3741.repositorys;

import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.Random;

public class UserRepositoryImpl {
    private final AppDatabase db;

    public UserRepositoryImpl(AppDatabase db) {
        this.db = db;
    }

    public User saveUser(String userName){
        Random rand = new Random();
        String key = String.format("%04d", rand.nextInt(10000));
        User user = new User(key, userName);
        long id = db.userDao().insert(user);
        user.setId(id);
        return user;
    }
}
