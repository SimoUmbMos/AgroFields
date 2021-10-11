package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandMemo;

import java.util.List;

@Dao
public interface LandMemoDao {
    @Query("SELECT * FROM LandMemos " +
            "WHERE UserID = :uid")
    List<LandMemo> getUserLandMemo(long uid);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandMemo landMemo);

    @Query("DELETE FROM LandMemos " +
            "WHERE UserID = :uid " +
            "AND LandID = :lid")
    void deleteByUserAndLand(long uid, long lid);
    @Delete
    void delete(LandMemo landMemo);
}
