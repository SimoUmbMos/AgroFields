package com.mosc.simo.ptuxiaki3741.backend.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query("SELECT r.* FROM LandDataRecord r " +
            "WHERE r.LandYear = :year " +
            "ORDER BY r.LandID, r.Date, r.LandTitle")
    List<LandDataRecord> getLandRecords(long year);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandDataRecord landRecord);

    @Delete
    void delete(LandDataRecord landRecord);
}
