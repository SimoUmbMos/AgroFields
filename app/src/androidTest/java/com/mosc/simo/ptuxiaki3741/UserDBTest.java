package com.mosc.simo.ptuxiaki3741;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class UserDBTest {
    public static final String TAG = "UserDBTest";
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
        testEditUsers();
        testCreateFriendRequests();
        testDeclineFriendRequests();
        testCreateFriends();
        testBlock();
        testUserSearch();
    }

    private void testCreateUsers() {
        User temp;
        for(int i = 0; i < 6; i++){
            temp = new User("test"+(i+1), "test"+(i+1));
            userRepository.saveNewUser(temp);
            userRepository.saveNewUser(EncryptUtil.encryptWithPassword(temp));
        }
       users.clear();
      users.addAll(userRepository.getUsers());
       assertEquals(6,users.size());
       for(User user : users){
            assertEquals(-1,user.getPassword().indexOf("user"));
        }
    }
    private void testEditUsers() {
        String email = "test@test.com";
        String phone = "";

        User temp = users.get(0);
        String firstPassword = temp.getPassword();
        temp.setEmail(email);
        temp.setPhone(phone);
        User encrypted = EncryptUtil.encrypt(temp);
        assertNotEquals(encrypted.getEmail(), email);
        assertNotEquals(encrypted.getPhone(), phone);
        assertEquals(firstPassword,encrypted.getPassword());

        User decrypted = EncryptUtil.decrypt(temp);
        assertEquals(decrypted.getEmail(), email);
        assertEquals(decrypted.getPhone(), phone);
        assertEquals(firstPassword,decrypted.getPassword());

        Log.d(TAG, "temp password: "+temp.getPassword());
        Log.d(TAG, "encrypted password: "+encrypted.getPassword());
        Log.d(TAG, "decrypted password: "+decrypted.getPassword());

        Log.d(TAG, "temp email: "+temp.getEmail());
        Log.d(TAG, "encrypted email: "+encrypted.getEmail());
        Log.d(TAG, "decrypted email: "+decrypted.getEmail());

        Log.d(TAG, "temp phone: "+temp.getPhone());
        Log.d(TAG, "encrypted phone: "+encrypted.getPhone());
        Log.d(TAG, "decrypted phone: "+decrypted.getPhone());
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
        long singlePageSpeed, singleDataSpeed, pageSpeed, dataSpeed;

        long start = System.currentTimeMillis();
        long pages0 = userRepository.searchPageCount(users.get(0),"test");
        singlePageSpeed = System.currentTimeMillis();
        long pages1 = userRepository.searchPageCount(users.get(1),"test");
        long pages2 = userRepository.searchPageCount(users.get(2),"test");
        long pages3 = userRepository.searchPageCount(users.get(3),"test");
        long pages4 = userRepository.searchPageCount(users.get(4),"test");
        long pages5 = userRepository.searchPageCount(users.get(5),"test");
        long pagesNull = userRepository.searchPageCount(users.get(0),"nul");
        long pagesNotNull = userRepository.searchPageCount(users.get(0),"null");
        pageSpeed = System.currentTimeMillis();

        singlePageSpeed = singlePageSpeed - start;
        pageSpeed = pageSpeed - start;

        assertEquals(1,pages0); //1
        assertEquals(1,pages1); //2
        assertEquals(1,pages2); //3
        assertEquals(1,pages3); //4
        assertEquals(1,pages4); //5
        assertEquals(1,pages5); //6
        assertEquals(-1,pagesNull); //6
        assertEquals(0,pagesNotNull); //6

        start = System.currentTimeMillis();
        List<User> result0 = userRepository.userSearch(users.get(0),"test",0);
        singleDataSpeed = System.currentTimeMillis();
        List<User> result1 = userRepository.userSearch(users.get(1),"test",0);
        List<User> result2 = userRepository.userSearch(users.get(2),"test",0);
        List<User> result3 = userRepository.userSearch(users.get(3),"test",0);
        List<User> result4 = userRepository.userSearch(users.get(4),"test",0);
        List<User> result5 = userRepository.userSearch(users.get(5),"test",0);
        List<User> resultNull = userRepository.userSearch(users.get(0),"nul",0);
        List<User> resultNotNull = userRepository.userSearch(users.get(0),"null",0);
        dataSpeed = System.currentTimeMillis();

        singleDataSpeed = singleDataSpeed - start;
        dataSpeed = dataSpeed - start;

        assertEquals(5,result0.size()); //1
        assertEquals(5,result1.size()); //2
        assertEquals(3,result2.size()); //3
        assertEquals(3,result3.size()); //4
        assertEquals(4,result4.size()); //5
        assertEquals(4,result5.size()); //6
        assertNull(resultNull);
        assertNotNull(resultNotNull);

        Log.d(TAG, "single page speeds: "+singlePageSpeed+"ms");
        Log.d(TAG, "all page speeds: "+pageSpeed+"ms");
        Log.d(TAG, "single data speeds: "+singleDataSpeed+"ms");
        Log.d(TAG, "all data speeds: "+dataSpeed+"ms");

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
