package com.mosc.simo.ptuxiaki3741.tests_db;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.UserRepository;
import com.mosc.simo.ptuxiaki3741.util.LandTestUtil;
import com.mosc.simo.ptuxiaki3741.util.TestUtil;
import com.mosc.simo.ptuxiaki3741.util.UserTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DBLandTest{
    //todo: complete & run tests
    private UserRepository userRepository;
    private LandRepository landRepository;
    private RoomDatabase db;
    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        userRepository = new UserRepositoryImpl(db);
        landRepository = new LandRepositoryImpl(db);
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
