package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public interface UserRepository {
    User getUserByID(long id);
    User getUserByUserName(String username);
    User getUserByUserNameAndPassword(String username, String password);
    List<User> userSearch(User user, String Username, int page);
    int searchUserMaxPage(User user, String Username);

    UserFriendRequestStatus sendFriendRequest(User currUser, User receiver);
    boolean deleteFriendRequest(User currUser, User receiver);
    boolean acceptFriendRequest(User currUser, User sender);
    boolean declineFriendRequest(User currUser, User sender);
    boolean deleteFriend(User currUser, User friend);
    void blockUser(User currUser, User otherUser);
    void removeBlock(User currUser, User otherUser);

    List<User> getUserFriendList(User user);
    List<User> getUserInboxRequestList(User user);
    List<User> getUserOutboxRequestList(User user);
    List<User> getUserBlockList(User user);

    User saveNewUser(User user);
    LoginRegisterError getNewUserError(User newUser);
    void editUser(User user);
    void deleteUser(User user);
}
