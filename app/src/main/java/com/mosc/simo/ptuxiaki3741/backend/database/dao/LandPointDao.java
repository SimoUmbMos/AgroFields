package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;

import java.util.List;

@Dao
public interface LandPointDao {
    @Query("SELECT * FROM `LandPoints`")
    List<LandPoint> getLandPoints();

    @Query("SELECT * FROM `LandPoints` Where `LandID` = :lid ORDER BY `Position` ASC")
    List<LandPoint> getAllLandPointsByLid(long lid);

    @Query("DELETE FROM `LandPoints` Where `LandID` = :lid")
    void deleteAllByLID(long lid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandPoint landPoint);
}
