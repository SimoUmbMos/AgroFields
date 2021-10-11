package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.LandMemo;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserMemo;

import java.util.List;

public interface UserRepository {

    User getUserByID(long id);
    User getUserByUserName(String username);
    User getUserByUserNameAndPassword(String username, String password);
    List<UserMemo> getUserFriendsMemosList(User user);
    List<LandMemo> getUserLandsMemosList(User user);
    List<User> userSearch(User user, String Username);

    UserFriendRequestStatus sendFriendRequest(User currUser, User receiver);
    boolean acceptFriendRequest(User currUser, User sender);
    boolean declineFriendRequest(User currUser, User sender);
    boolean deleteFriend(User currUser, User friend);
    void blockUser(User currUser, User otherUser);
    void removeBlock(User currUser, User otherUser);

    List<User> getUserFriendList(User user);
    List<User> getUserFriendRequestList(User user);
    List<User> getUserBlockList(User user);

    User saveNewUser(User user);
    void editUser(User user);
    void deleteUser(User user);
}
