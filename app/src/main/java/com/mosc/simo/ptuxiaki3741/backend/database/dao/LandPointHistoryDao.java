package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.LandPointRecord;

import java.util.List;

@Dao
public interface LandPointHistoryDao {
    @Query("SELECT * FROM `LandPointsRecord` Where `id` = :id")
    LandPointRecord getLandPointHistoryById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<LandPointRecord> landPointRecords);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandPointRecord landPointRecords);

    @Delete
    int delete(LandPointRecord landPointRecord);
}
