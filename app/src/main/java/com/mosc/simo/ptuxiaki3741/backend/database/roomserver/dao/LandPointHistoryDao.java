package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandPointRecord;

import java.util.List;

@Dao
public interface LandPointHistoryDao {
    @Query("SELECT * FROM `LandPointsRecord`")
    List<LandPointRecord> getLandPointsHistory();

    @Query("SELECT * FROM `LandPointsRecord` Where `LandRecordID` = :LRid ORDER BY `Position` ASC")
    List<LandPointRecord> getLandPointHistoryByLRID(long LRid);

    @Query("DELETE FROM `LandPointsRecord` Where `LandRecordID` = :LRid")
    void deleteAllByLRID(long LRid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandPointRecord landPoint);
}
