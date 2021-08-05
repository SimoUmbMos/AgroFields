package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

public interface UserRelationshipRepository {
    List<User> getUserFriendList(User user);
    List<User> getUserFriendRequestList(User user);
    List<User> getUserBlockList(User user);

    boolean createUserRelationship(User user1, User user2, UserDBAction type);
    void deleteUserRelationship(User user1, User user2);
}
