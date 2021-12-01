package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;

import java.util.List;

@Dao
public interface LandZoneDao {
    @Query("SELECT * FROM LandZoneData")
    List<LandZoneData> getZones();

    @Query("SELECT * FROM LandZoneData " +
            "WHERE LandID = :lid")
    List<LandZoneData> getLandZonesByLandID(long lid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandZoneData landMemo);
    @Delete
    void delete(LandZoneData land);
    @Query("DELETE FROM LandZoneData " +
            "WHERE LandID = :lid")
    void deleteByLID(long lid);
}
