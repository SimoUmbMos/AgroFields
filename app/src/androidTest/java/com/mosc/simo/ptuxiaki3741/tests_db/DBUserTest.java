package com.mosc.simo.ptuxiaki3741.tests_db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.UserRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DBUserTest {
    //todo: complete & run tests
    private UserRepository userRepository;
    private RoomDatabase db;
    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        userRepository = new UserRepositoryImpl(db);
    }
    @After
    public void closeDb() {
        db.close();
    }
    @Test
    public void test() {
        //code..
    }
}
