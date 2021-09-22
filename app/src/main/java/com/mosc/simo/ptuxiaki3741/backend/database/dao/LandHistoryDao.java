package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomValues;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query(RoomValues.LandHistoryDaoValues.GetLandRecordByLandID)
    List<LandDataRecord> getLandRecordByLandID(long lid);

    @Query(RoomValues.LandHistoryDaoValues.GetLandRecordsByUserId)
    List<LandDataRecord> getLandRecordsByUserId(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandDataRecord landRecord);

    @Delete
    void delete(LandDataRecord landRecord);
}
