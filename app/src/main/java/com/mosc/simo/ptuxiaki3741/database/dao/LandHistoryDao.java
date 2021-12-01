package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query("SELECT r.* FROM LandDataRecord r ORDER BY r.LandID, r.Date, r.LandTitle")
    List<LandDataRecord> getLandRecords();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandDataRecord landRecord);

    @Delete
    void delete(LandDataRecord landRecord);
}
