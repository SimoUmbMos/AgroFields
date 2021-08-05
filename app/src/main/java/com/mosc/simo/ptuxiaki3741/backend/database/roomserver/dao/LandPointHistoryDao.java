package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.LandPointRecord;

import java.util.List;

@Dao
public interface LandPointHistoryDao {
    @Query("SELECT * FROM `LandPointsRecord`")
    List<LandPointRecord> getLandPointsHistory();

    @Query("SELECT * FROM `LandPointsRecord` Where `LRid` = :LRid ORDER BY `Position` ASC")
    List<LandPointRecord> getLandPointHistoryByLRID(long LRid);

    @Query("DELETE FROM `LandPointsRecord` Where `LRid` = :LRid")
    void deleteAllByLRID(long LRid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandPointRecord landPoint);
}
