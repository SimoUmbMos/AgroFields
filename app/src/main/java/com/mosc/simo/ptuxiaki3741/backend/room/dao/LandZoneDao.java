package com.mosc.simo.ptuxiaki3741.backend.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;

import java.util.List;

@Dao
public interface LandZoneDao {
    @Query(
            "SELECT * " +
            "FROM LandZoneData "
    )
    List<LandZoneData> getZones();

    @Query(
            "SELECT LandZoneData.* " +
            "FROM LandZoneData INNER JOIN LandData " +
            "ON LandZoneData.LandID = LandData.ID " +
            "WHERE LandData.Year = :year "+
            "ORDER BY ID "
    )
    List<LandZoneData> getZones(long year);

    @Query(
            "SELECT EXISTS( " +
                "SELECT * " +
                "FROM LandZoneData " +
                "WHERE ID = :id " +
            ") "
    )
    boolean zoneExist(long id);

    @Query(
            "SELECT * " +
            "FROM LandZoneData " +
            "WHERE LandZoneData.LandID = :lid "+
            "ORDER BY ID "
    )
    List<LandZoneData> getLandZonesByLandID(long lid);


    @Query(
            "SELECT * " +
            "FROM LandZoneData " +
            "WHERE LandZoneData.ID = :id "+
            "LIMIT 1 "
    )
    LandZoneData getLandZone(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandZoneData zone);
    @Delete
    void delete(LandZoneData zone);
}
