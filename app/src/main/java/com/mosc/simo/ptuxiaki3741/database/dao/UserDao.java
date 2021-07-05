package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM `Users` WHERE `id` = :id")
    User getUserById(long id);

    @Query("SELECT * FROM `Users` WHERE `Key` = :key")
    User getUserByKey(int key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    int delete(User user);
}
