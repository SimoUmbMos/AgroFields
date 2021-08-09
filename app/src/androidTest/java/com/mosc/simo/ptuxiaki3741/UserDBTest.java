package com.mosc.simo.ptuxiaki3741;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class UserDBTest {
    private static final String TAG = "UserJUnitTest";

    private RoomDatabase db;
    private UserRepositoryImpl userRepository;

    private List<User> users;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        userRepository = new UserRepositoryImpl(db);
        initUsers();
        initRelationships();
    }

    @After
    public void closeDb(){
        db.close();
    }

    @Test
    public void userFriendTest() {
        List<User> user1Friends = userRepository.getUserFriendList(users.get(0));
        List<User> user2Friends = userRepository.getUserFriendList(users.get(1));

        debugList("user1Friends",user1Friends);
        debugList("user2Friends",user2Friends);

        assertEquals(1,user1Friends.size());
        assertEquals(3,user2Friends.size());
    }
    @Test
    public void userRequestTest() {
        List<User> user1FriendRequest = userRepository.getUserFriendRequestList(users.get(0));
        List<User> user3FriendRequest = userRepository.getUserFriendRequestList(users.get(2));

        debugList("user1FriendRequest",user1FriendRequest);
        debugList("user3FriendRequest",user3FriendRequest);

        assertEquals(0,user1FriendRequest.size());
        assertEquals(1,user3FriendRequest.size());
    }
    @Test
    public void userBlockTest() {
        List<User> user1BlockList = userRepository.getUserBlockList(users.get(0));
        List<User> user2BlockList = userRepository.getUserBlockList(users.get(1));
        List<User> user3BlockList = userRepository.getUserBlockList(users.get(2));

        debugList("user1BlockList",user1BlockList);
        debugList("user2BlockList",user2BlockList);
        debugList("user3BlockList",user3BlockList);

        assertEquals(2,user1BlockList.size());
        assertEquals(1,user2BlockList.size());
        assertEquals(0,user3BlockList.size());
    }
    @Test
    public void userSearchTest() {
        List<User> user1SearchList = userRepository.userSearch(users.get(0),"user");
        List<User> user2SearchList = userRepository.userSearch(users.get(1),"user");
        List<User> user3SearchList = userRepository.userSearch(users.get(2),"user");

        debugList("user1SearchList",user1SearchList);
        debugList("user2SearchList",user2SearchList);
        debugList("user3SearchList",user3SearchList);

        assertEquals(3,user1SearchList.size());
        assertEquals(4,user2SearchList.size());
        assertEquals(5,user3SearchList.size());
    }

    private void initUsers() {
        User temp;
        for(int i = 1 ; i <= 6 ; i++){
            temp = new User("user"+i,"user"+i);
            userRepository.saveNewUser(temp);
        }
        users = userRepository.getUsers();
    }
    private void initRelationships() {
        userRepository.saveUserRelationship(users.get(0),users.get(1),UserDBAction.FRIENDS);  // 1 is friends 2
        userRepository.saveUserRelationship(users.get(0),users.get(2),UserDBAction.REQUESTED);// 1 has request friend 3
        userRepository.saveUserRelationship(users.get(0),users.get(3),UserDBAction.BLOCKED);  // 1 has blocked 4
        userRepository.saveUserRelationship(users.get(0),users.get(4),UserDBAction.BLOCKED);  // 1 has blocked 5

        userRepository.saveUserRelationship(users.get(1),users.get(2),UserDBAction.FRIENDS);  // 2 is friends 3
        userRepository.saveUserRelationship(users.get(1),users.get(3),UserDBAction.FRIENDS);  // 2 is friends 4
        userRepository.saveUserRelationship(users.get(1),users.get(5),UserDBAction.BLOCKED);  // 2 has blocked 6
    }
    private void debugList(String name,List<User> usersLists){
        Log.d(TAG, name+" size:" +usersLists.size());
        if(usersLists.size() > 0){
            for(User user : usersLists){
                Log.d(TAG, user.getUsername());
            }
        }
        Log.d(TAG, " ");
    }
}
