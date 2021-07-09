package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM `Users`")
    List<User> getUsers();

    @Query("SELECT * FROM `Users` WHERE `id` = :id")
    User getUserById(long id);

    @Query("SELECT * FROM `Users` WHERE `Key` = :key")
    List<User> getUsersByKey(String key);

    @Query("SELECT * FROM `Users` WHERE `Username` = :username")
    List<User> getUsersByUserName(String username);

    @Query("SELECT * FROM `Users` WHERE `Key` = :key AND `Username` = :username")
    List<User> getAccurateUsersByKeyAndUserName(String key, String username);

    @Query("SELECT * FROM `Users` WHERE `Key` = :key OR `Username` = :username")
    List<User> getUsersByKeyAndUserName(String key, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    void delete(User user);
}
