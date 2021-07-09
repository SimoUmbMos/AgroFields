package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

public interface UserRepository {
    User searchUserByID(long id);
    List<User> searchUserByKey(String key);
    List<User> searchUserByUserName(String username);
    List<User> getUsersByKeyAndUserName(String key,String username);
    List<User> getUsers();
    User saveUser(User user);
    void deleteUser(User user);
}
