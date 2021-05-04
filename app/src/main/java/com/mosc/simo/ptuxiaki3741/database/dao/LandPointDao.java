package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;

import java.util.List;

@Dao
public interface LandPointDao {
    @Query("SELECT * FROM `LandPoints` Where `id` = :id")
    LandPoint getLandPointById(long id);

    @Query("SELECT * FROM `LandPoints` Where `Lid` = :lid ORDER BY `Position` ASC")
    List<LandPoint> getAllLandPointsByLid(long lid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandPoint landPoint);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<LandPoint> landPoints);

    @Delete
    int delete(LandPoint landPoint);

    @Delete
    int deleteAll(List<LandPoint> landPoint);
}
