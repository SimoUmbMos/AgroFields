package com.mosc.simo.ptuxiaki3741;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UserUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UserDBTest {
    private RoomDatabase db;
    private UserRepositoryImpl userRepository;
    private List<User> users;
    private boolean firstStart = true;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        userRepository = new UserRepositoryImpl(db);
        users = new ArrayList<>();
    }

    @After
    public void closeDb(){
        db.close();
    }

    @Test
    public void testAll(){
        if(firstStart){
            firstStart = false;
        }else{
            resetDB();
        }

        testCreateUsers();
        testCreateFriendRequests();
        testDeclineFriendRequests();
        testCreateFriends();
        testBlock();
        testUserSearch();
    }

    private void testCreateUsers() {
        User temp;
        for(int i = 0 ; i < 6 ; i++){
            temp = new User("user"+(i+1), UserUtil.hashPassword("user"+(i+1)));
            userRepository.saveNewUser(temp);
        }
        users.clear();
        users.addAll(userRepository.getUsers());
        assertEquals(6,users.size());
        for(User user : users){
            assertEquals(-1,user.getPassword().indexOf("user"));
        }
    }
    private void testCreateFriendRequests(){
        userRepository.sendFriendRequest(users.get(1),users.get(0));  // 2 has request friend 1
        userRepository.sendFriendRequest(users.get(2),users.get(0));  // 3 has request friend 1

        userRepository.sendFriendRequest(users.get(2),users.get(1));  // 3 has request friend 2
        userRepository.sendFriendRequest(users.get(3),users.get(1));  // 4 has request friend 2
        userRepository.sendFriendRequest(users.get(5),users.get(1));  // 6 has request friend 2

        checkFriendRequestList(2,users.get(0));
        checkFriendRequestList(3,users.get(1));
        checkFriendRequestList(0,users.get(2));
        checkFriendRequestList(0,users.get(3));
        checkFriendRequestList(0,users.get(4));
        checkFriendRequestList(0,users.get(5));
    }
    private void testDeclineFriendRequests(){
        userRepository.declineFriendRequest(users.get(0),users.get(2));  // 1 has decline friend 3
        userRepository.declineFriendRequest(users.get(1),users.get(3));  // 2 has decline friend 4
        userRepository.declineFriendRequest(users.get(1),users.get(5));  // 2 has decline friend 6

        checkFriendRequestList(1,users.get(0));
        checkFriendRequestList(1,users.get(1));
        checkFriendRequestList(0,users.get(2));
        checkFriendRequestList(0,users.get(3));
        checkFriendRequestList(0,users.get(4));
        checkFriendRequestList(0,users.get(5));
    }
    private void testCreateFriends(){
        userRepository.acceptFriendRequest(users.get(0),users.get(1));  // 1 is friends 2
        userRepository.acceptFriendRequest(users.get(1),users.get(2));  // 2 is friends 3

        checkFriendList(1,users.get(0));
        checkFriendList(2,users.get(1));
        checkFriendList(1,users.get(2));
        checkFriendList(0,users.get(3));
        checkFriendList(0,users.get(4));
        checkFriendList(0,users.get(5));
    }
    private void testBlock() {
        userRepository.blockUser(users.get(2),users.get(3));// 3 has blocked 4
        userRepository.blockUser(users.get(2),users.get(4));// 3 has blocked 5
        userRepository.blockUser(users.get(3),users.get(5));// 4 has blocked 6

        checkBlockList(0,users.get(0));
        checkBlockList(0,users.get(1));
        checkBlockList(2,users.get(2));
        checkBlockList(1,users.get(3));
        checkBlockList(0,users.get(4));
        checkBlockList(0,users.get(5));
    }
    private void testUserSearch() {
        List<User> result0 = userRepository.userSearch(users.get(0),"user");
        List<User> result1 = userRepository.userSearch(users.get(1),"user");
        List<User> result2 = userRepository.userSearch(users.get(2),"user");
        List<User> result3 = userRepository.userSearch(users.get(3),"user");
        List<User> result4 = userRepository.userSearch(users.get(4),"user");
        List<User> result5 = userRepository.userSearch(users.get(5),"user");

        assertEquals(5,result0.size());
        assertEquals(5,result1.size());
        assertEquals(3,result2.size());
        assertEquals(3,result3.size());
        assertEquals(4,result4.size());
        assertEquals(4,result5.size());
    }

    private void checkFriendList(int size,User user){
        List<User> userFriends = userRepository.getUserFriendList(user);
        assertEquals(size,userFriends.size());
    }
    private void checkFriendRequestList(int size,User user){
        List<User> userFriendRequests = userRepository.getUserFriendRequestList(user);
        assertEquals(size,userFriendRequests.size());
    }
    private void checkBlockList(int size,User user){
        List<User> userBlocks = userRepository.getUserBlockList(user);
        assertEquals(size,userBlocks.size());
    }

    private void resetDB(){
        closeDb();
        createDb();
    }
}
