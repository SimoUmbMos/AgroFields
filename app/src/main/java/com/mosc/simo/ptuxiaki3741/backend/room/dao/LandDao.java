package com.mosc.simo.ptuxiaki3741.backend.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;

import java.util.List;

@Dao
public interface LandDao {
    @Query(
            "SELECT EXISTS( " +
                    "SELECT * " +
                    "FROM LandData " +
                    "WHERE ID = :id " +
            ")"
    )
    boolean landExist(long id);

    @Query(
            "SELECT DISTINCT Year " +
            "FROM LandData "
    )
    List<Long> getYears();

    @Query(
            "SELECT * FROM LandData " +
            "WHERE ID = :id "
    )
    LandData getLand(long id);


    @Query("SELECT * FROM LandData")
    List<LandData> getLands();

    @Query(
            "SELECT * FROM LandData " +
            "WHERE LandData.Year = :year " +
            "ORDER BY ID"
    )
    List<LandData> getLands(long year);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandData land);

    @Delete
    void delete(LandData land);
}
