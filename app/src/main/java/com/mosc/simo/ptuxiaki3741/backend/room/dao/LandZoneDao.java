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
            "SELECT * FROM LandZoneData " +
            "WHERE LandZoneData.Snapshot = :snapshot "+
            "ORDER BY ID"
    )
    List<LandZoneData> getZones(long snapshot);

    @Query(
            "SELECT EXISTS( " +
                "SELECT * FROM LandZoneData " +
                "WHERE ID = :id AND LandZoneData.Snapshot = :snapshot " +
            ")"
    )
    boolean zoneExist(long id, long snapshot);

    @Query(
            "SELECT * FROM LandZoneData " +
            "WHERE LandZoneData.LandID = :lid AND LandZoneData.Snapshot = :snapshot "+
            "ORDER BY ID"
    )
    List<LandZoneData> getLandZonesByLandID(long lid, long snapshot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandZoneData zone);
    @Delete
    void delete(LandZoneData zone);
}
