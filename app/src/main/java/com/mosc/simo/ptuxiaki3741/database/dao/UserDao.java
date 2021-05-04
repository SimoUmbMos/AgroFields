package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.database.model.LandOwner;
import com.mosc.simo.ptuxiaki3741.database.model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM `Users` WHERE `id` = :id")
    User getUserById(long id);

    @Query("SELECT * FROM `Users` WHERE `Username` = '%' || :username || '%' AND `Key` = :key")
    User getUserByUsernameAndKey(String username,int key);

    @Query("SELECT * FROM `Users` WHERE `Username` LIKE '%' || :username || '%'")
    List<User> getUsersByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    int delete(User user);
}
