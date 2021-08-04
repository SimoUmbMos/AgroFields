package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

public interface UserRepository {
    List<User> getUsers();
    User searchUserByID(long id);
    User searchUserByUserName(String username);
    User saveNewUser(User user);
    void editUser(User user);
    void deleteUser(User user);
}
