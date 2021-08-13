package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public interface UserRepository {

    User searchUserByID(long id);
    User searchUserByUserName(String username);
    User searchUserByUserNameAndPassword(String username, String password);
    List<User> userSearch(User user, String Username);

    boolean sendFriendRequest(User currUser, User receiver);
    boolean acceptFriendRequest(User currUser, User sender);
    boolean declineFriendRequest(User currUser, User sender);
    void blockUser(User currUser, User otherUser);

    List<User> getUsers();
    List<User> getUserFriendList(User user);
    List<User> getUserFriendRequestList(User user);
    List<User> getUserBlockList(User user);

    User saveNewUser(User user);
    void editUser(User user);
    void deleteUser(User user);

}
