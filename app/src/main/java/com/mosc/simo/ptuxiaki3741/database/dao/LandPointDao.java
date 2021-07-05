package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.LandPoint;

import java.util.List;

@Dao
public interface LandPointDao {

    @Query("SELECT * FROM `LandPoints` Where `Lid` = :lid ORDER BY `Position` ASC")
    List<LandPoint> getAllLandPointsByLid(long lid);

    @Query("DELETE FROM `LandPoints` Where `lid` = :lid")
    int deleteByLID(long lid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<LandPoint> landPoint);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandPoint landPoint);

    @Delete
    int delete(LandPoint landPoint);
}
