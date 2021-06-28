package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.database.model.LandRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query("SELECT * FROM `LandRecords` Where `UserId` = :uid")
    List<LandRecord> getLandsHistoryByUserId(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandRecord landRecord);

    @Delete
    int delete(LandRecord landRecord);
}
