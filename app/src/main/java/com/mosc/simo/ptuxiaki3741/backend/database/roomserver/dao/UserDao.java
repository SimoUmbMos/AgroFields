package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM `Users`")
    List<User> getUsers();

    @Query("SELECT * FROM `Users` WHERE `Username` LIKE '%' || :username || '%' AND id != :id")
    List<User> searchUserByUserName(long id, String username);

    @Query("SELECT * FROM `Users` WHERE `id` = :id")
    User getUserById(long id);

    @Query("SELECT * FROM `Users` WHERE `Username` = :username")
    User getUserByUserName(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    void delete(User user);

}
