package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

import java.util.List;

public interface UserRepository {

    User searchUserByID(long id);
    User searchUserByUserName(String username);
    List<User> userSearch(User user, String Username);

    List<User> getUsers();
    List<User> getUserFriendList(User user);
    List<User> getUserFriendRequestList(User user);
    List<User> getUserBlockList(User user);

    User saveNewUser(User user);
    void saveUserRelationship(User user1, User user2, UserDBAction type);
    void saveUserRelationship(UserRelationship userRelationship);

    void editUser(User user);

    void deleteUser(User user);
    void deleteUserRelationship(User user1, User user2);
    void deleteUserRelationship(long userID1, long userID2);
}
