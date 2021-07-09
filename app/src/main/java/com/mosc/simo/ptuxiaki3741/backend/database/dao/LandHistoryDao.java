package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.LandDataRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query("SELECT * FROM LandDataRecord Where `id` = :id")
    LandDataRecord getLandRecord(long id);

    @Query("SELECT * FROM LandDataRecord Where `CreatorId` = :uid")
    List<LandDataRecord> getLandRecordsByUserId(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandDataRecord landRecord);

    @Delete
    int delete(LandDataRecord landRecord);
}
