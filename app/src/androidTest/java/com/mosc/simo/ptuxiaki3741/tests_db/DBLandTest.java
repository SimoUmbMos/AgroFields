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
    /*todo: complete
     * save Land
     * edit Land
     * remove Land
     * add share Land
     * remove share Land
     * change owner Lands
     * load user Lands
     * save history Lands
     * restore history Lands
    */
    //todo: run tests
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
    public void testSaveLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
    @Test
    public void testEditLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
    @Test
    public void testRemoveLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
    @Test
    public void testLoadUserLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        List<Land> lands = new ArrayList<>();
        for(int i = 0; i < 10;i++){
            lands.add(
                    LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user)
            );
        }

    }

    @Test
    public void testAddShareLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        User contact = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
    @Test
    public void testRemoveShareLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        User contact = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
    @Test
    public void testChangeOwnerLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        User contact = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }

    @Test
    public void testSaveHistoryLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
    @Test
    public void testRestoreHistoryLand(){
        User user = UserTestUtil.createMockUser(TestUtil.getRandomAlphaNumericString(8));
        Land land = LandTestUtil.createMockLand(TestUtil.getRandomAlphaNumericString(8), user);
    }
}
