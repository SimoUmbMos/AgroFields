package com.mosc.simo.ptuxiaki3741.backend.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;

import java.util.List;

@Dao
public interface LandZoneHistoryDao {
    @Query(
            "SELECT * FROM LandZoneDataRecord " +
            "WHERE RecordID = :id AND RecordSnapshot = :snapshot "+
            "ORDER BY ID"
    )
    List<LandZoneDataRecord> getZoneRecordByRecordIdAndSnapshot(long id, long snapshot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandZoneDataRecord record);

    @Delete
    void delete(LandZoneDataRecord record);
}
