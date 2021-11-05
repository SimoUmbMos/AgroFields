package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandMemo;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserMemo;

import java.util.List;

public interface UserRepository {
    User getUserByID(long id);
    User getUserByUserName(String username);
    User getUserByUserNameAndPassword(String username, String password);
    List<User> userSearch(User user, String Username);

    UserFriendRequestStatus sendFriendRequest(User currUser, User receiver);
    boolean deleteFriendRequest(User currUser, User receiver);
    boolean acceptFriendRequest(User currUser, User sender);
    boolean declineFriendRequest(User currUser, User sender);
    boolean deleteFriend(User currUser, User friend);
    void blockUser(User currUser, User otherUser);
    void removeBlock(User currUser, User otherUser);

    LandMemo saveMemo(User currUser, LandData landData, String memo);
    UserMemo saveMemo(User currUser, User contact, String memo);
    void removeMemo(User currUser, LandData landData);
    void removeMemo(User currUser, User contact);
    void saveMemo(LandMemo memo);
    void saveMemo(UserMemo memo);
    void removeMemo(LandMemo memo);
    void removeMemo(UserMemo memo);

    List<User> getUserFriendList(User user);
    List<User> getUserInboxRequestList(User user);
    List<User> getUserOutboxRequestList(User user);
    List<User> getUserBlockList(User user);
    List<UserMemo> getUserFriendsMemosList(User user);
    List<LandMemo> getUserLandsMemosList(User user);

    User saveNewUser(User user);
    void editUser(User user);
    void deleteUser(User user);
}
