package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRelationshipRepository;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.models.UserRelationship;

import java.util.ArrayList;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil.getAllReceiverUsers;
import static com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil.getAllSenderUsers;

public class UserRelationshipRepositoryImpl implements UserRelationshipRepository {
    private final RoomDatabase db;
    public UserRelationshipRepositoryImpl(RoomDatabase db){
        this.db = db;
    }

    @Override
    public List<User> getUserFriendList(User user) {
        List<UserRelationship> senderRelationships =
                db.userRelationshipDao().getUserRelationshipBySenderIDAndType(
                        user.getId(),
                        UserDBAction.FRIENDS
                );
        List<UserRelationship> receiverRelationships =
                db.userRelationshipDao().getUserRelationshipByReceiverIDAndType(
                        user.getId(),
                        UserDBAction.FRIENDS
                );
        List<User> friends = new ArrayList<>();
        friends.addAll(getAllSenderUsers(receiverRelationships, db));
        friends.addAll(getAllReceiverUsers(senderRelationships, db));
        return friends;
    }

    @Override
    public List<User> getUserFriendRequestList(User user) {
        List<UserRelationship> receiverRelationships =
                db.userRelationshipDao().getUserRelationshipByReceiverIDAndType(
                        user.getId(),
                        UserDBAction.REQUESTED
                );
        return getAllSenderUsers(receiverRelationships, db);
    }

    @Override
    public List<User> getUserBlockList(User user) {
        List<UserRelationship> SenderRelationships =
                db.userRelationshipDao().getUserRelationshipBySenderIDAndType(
                        user.getId(),
                        UserDBAction.BLOCKED
                );
        return getAllReceiverUsers(SenderRelationships, db);
    }

    @Override
    public boolean createUserRelationship(User sender, User receiver, UserDBAction type) {
        return false;
    }

    @Override
    public void deleteUserRelationship(User user1, User user2) {

    }

}
