package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.UserRelationshipRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil;

import java.util.Date;
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
        List<UserRelationship> friendSenderRelationship = db.userRelationshipDao()
                .getBySenderIDAndType(user.getId(), UserDBAction.FRIENDS);
        List<UserRelationship> friendReceiverRelationship = db.userRelationshipDao()
                .getByReceiverIDAndType(user.getId(), UserDBAction.FRIENDS);

        List<User> friends = UserRelationshipUtil
                .getAllReceiverUsers(friendSenderRelationship, db);
        friends.addAll(UserRelationshipUtil
                .getAllSenderUsers(friendReceiverRelationship, db));

        return friends;
    }

    @Override
    public List<User> getUserFriendRequestList(User user) {
        List<UserRelationship> requestRelationship = db.userRelationshipDao()
                .getByReceiverIDAndType(user.getId(), UserDBAction.REQUESTED);
        return getAllSenderUsers(requestRelationship, db);
    }

    @Override
    public List<User> getUserBlockList(User user) {
        List<UserRelationship> blockRelationship = db.userRelationshipDao()
                .getBySenderIDAndType(user.getId(), UserDBAction.BLOCKED);
        return getAllReceiverUsers(blockRelationship, db);
    }

    @Override
    public void createUserRelationship(User sender, User receiver, UserDBAction type) {
        db.userRelationshipDao().insert(
                new UserRelationship(sender.getId(),receiver.getId(), type, new Date())
        );
    }

    @Override
    public void deleteUserRelationship(User user1, User user2) {

    }

}
