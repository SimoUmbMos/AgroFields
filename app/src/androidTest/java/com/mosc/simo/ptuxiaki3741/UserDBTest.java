package com.mosc.simo.ptuxiaki3741;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.models.UserRelationship;
import com.mosc.simo.ptuxiaki3741.util.UserRelationshipUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class UserDBTest {
    private RoomDatabase db;
    private User user1, user2;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        user1 = new User("user1","user1");
        user2 = new User("user2","user2");
        user1.setId(db.userDao().insert(user1));
        user2.setId(db.userDao().insert(user2));
    }

    @After
    public void closeDb(){
        db.close();
    }

    @Test
    public void userRequestTest() {
        db.userRelationshipDao().insert(
                new UserRelationship(user1.getId(),user2.getId(),UserDBAction.REQUESTED, new Date())
        );
        List<UserRelationship> user1ReceiverRelationship = db.userRelationshipDao()
                .getUserRelationshipByReceiverIDAndType(user1.getId(), UserDBAction.REQUESTED);
        List<UserRelationship> user2ReceiverRelationship = db.userRelationshipDao()
                .getUserRelationshipByReceiverIDAndType(user2.getId(), UserDBAction.REQUESTED);
        List<User> user1FriendRequest = UserRelationshipUtil
                .getAllSenderUsers(user1ReceiverRelationship, db);
        List<User> user2FriendRequest = UserRelationshipUtil
                .getAllSenderUsers(user2ReceiverRelationship, db);
        assertEquals(0,user1FriendRequest.size());
        assertEquals(1,user2FriendRequest.size());
        assertEquals(user1.getId(),user2FriendRequest.get(0).getId());
    }
    @Test
    public void userFriendTest() {
        db.userRelationshipDao().insert(
                new UserRelationship(user1.getId(),user2.getId(),UserDBAction.FRIENDS, new Date())
        );
        List<UserRelationship> user1SenderRelationship = db.userRelationshipDao()
                .getUserRelationshipBySenderIDAndType(user1.getId(), UserDBAction.FRIENDS);
        List<UserRelationship> user1ReceiverRelationship = db.userRelationshipDao()
                .getUserRelationshipByReceiverIDAndType(user1.getId(), UserDBAction.FRIENDS);
        List<User> user1Friends = UserRelationshipUtil
                .getAllReceiverUsers(user1SenderRelationship, db);
        user1Friends.addAll(UserRelationshipUtil
                .getAllSenderUsers(user1ReceiverRelationship, db));
        List<UserRelationship> user2SenderRelationship = db.userRelationshipDao()
                .getUserRelationshipBySenderIDAndType(user2.getId(), UserDBAction.FRIENDS);
        List<UserRelationship> user2ReceiverRelationship = db.userRelationshipDao()
                .getUserRelationshipByReceiverIDAndType(user2.getId(), UserDBAction.FRIENDS);
        List<User> user2Friends = UserRelationshipUtil
                .getAllReceiverUsers(user2SenderRelationship, db);
        user2Friends.addAll(UserRelationshipUtil
                .getAllSenderUsers(user2ReceiverRelationship, db));
        assertEquals(1,user1Friends.size());
        assertEquals(user2.getId(),user1Friends.get(0).getId());
        assertEquals(1,user2Friends.size());
        assertEquals(user1.getId(),user2Friends.get(0).getId());
    }
    @Test
    public void userBlockTest() {
        db.userRelationshipDao().insert(
                new UserRelationship(user1.getId(),user2.getId(),UserDBAction.BLOCKED, new Date())
        );
        List<UserRelationship> user1BlockRelationship = db.userRelationshipDao()
                .getUserRelationshipBySenderIDAndType(user1.getId(), UserDBAction.BLOCKED);
        List<UserRelationship> user2BlockRelationship = db.userRelationshipDao()
                .getUserRelationshipBySenderIDAndType(user2.getId(), UserDBAction.BLOCKED);
        List<User> user1BlockList = UserRelationshipUtil
                .getAllReceiverUsers(user1BlockRelationship, db);
        List<User> user2BlockList = UserRelationshipUtil
                .getAllReceiverUsers(user2BlockRelationship, db);
        assertEquals(1,user1BlockList.size());
        assertEquals(user2.getId(),user1BlockList.get(0).getId());
        assertEquals(0,user2BlockList.size());
    }
}
