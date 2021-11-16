package com.mosc.simo.ptuxiaki3741.tests_view_model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import android.app.Application;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class UserViewModelTests {
    private static final String TAG = "UserViewModelTests";

    private UserViewModel vmUsers;
    private RoomDatabase db;
    @Before
    public void init(){
        Application app = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(app, RoomDatabase.class).build();
        vmUsers = new UserViewModel(app,db);
        User user = vmUsers.saveNewUser(
                new User("test","test","","test@test.com")
        );
        vmUsers.singIn(user);
    }
    @After
    public void finish(){
        db.close();
    }

    @Test
    public void test1_getCurrUser(){
        User user = vmUsers.getCurrUser().getValue();
        if(user == null){
            Log.v(TAG, "CurrUser is null");
            fail();
        }

        Log.v(TAG, "CurrUser id: " +user.getId());
        Log.v(TAG, "CurrUser Username: " +user.getUsername());
        Log.v(TAG, "CurrUser Password: " +user.getPassword());
        Log.v(TAG, "CurrUser Phone: " +user.getPhone());
        Log.v(TAG, "CurrUser Email: " +user.getEmail());

        assertEquals(user.getId(),1);
        assertEquals(user.getUsername(),"test");
        assertNotEquals(user.getPassword(),"test");
        assertEquals(user.getPhone(),"");
        assertEquals(user.getEmail(),"test@test.com");
    }
    @Test
    public void test2_searchUser(){
        User temp,temp2;
        for (int i = 0; i < 71; i++){
            temp = new User("temp"+i,"","temp"+i+"@test.com");
            temp2 = vmUsers.saveNewUser(temp);
            if(temp2 == null){
                Log.v(TAG, "user didn't save");
                fail();
            }
        }

        int maxPage = vmUsers.searchUserMaxPage("temp");
        assertEquals(3,maxPage);

        int page = -10;
        List<User> search = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        while(page <= maxPage){
            search.addAll(vmUsers.searchUser("temp",page));
            for(User user:search){
                builder.append(user.getId());builder.append(", ");
            }
            Log.v(TAG, "Page: " +page+", items: "+search.size());
            Log.v(TAG, "ids: "+builder.toString());

            builder.setLength(0);
            search.clear();
            page++;
        }
    }
}
