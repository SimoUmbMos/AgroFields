package com.mosc.simo.ptuxiaki3741.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.entities.LandDataRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query("SELECT r.* FROM LandDataRecord r " +
            "WHERE r.Snapshot = :snapshot " +
            "ORDER BY r.LandID, r.Date, r.LandTitle")
    List<LandDataRecord> getLandRecords(long snapshot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandDataRecord landRecord);

    @Delete
    void delete(LandDataRecord landRecord);
}
