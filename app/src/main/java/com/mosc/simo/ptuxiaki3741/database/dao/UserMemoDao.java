package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.UserMemo;

import java.util.List;

@Dao
public interface UserMemoDao {
    @Query("SELECT * FROM UserMemos " +
            "WHERE UserID = :uid")
    List<UserMemo> getUserLandMemo(long uid);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserMemo userMemo);
    @Delete
    void delete(UserMemo userMemo);
}
