package com.mosc.simo.ptuxiaki3741.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;

import java.util.List;

@Dao
public interface LandDao {
    @Query(
            "SELECT EXISTS( " +
                    "SELECT * FROM LandData " +
                    "WHERE ID = :id AND LandData.Snapshot = :snapshot " +
                    ")"
    )
    boolean landExist(long id, long snapshot);

    @Query(
            "SELECT * FROM LandData " +
            "WHERE ID = :id AND LandData.Snapshot = :snapshot "
    )
    LandData getLand(long id, long snapshot);

    @Query(
            "SELECT * FROM LandData " +
            "WHERE LandData.Snapshot = :snapshot " +
            "ORDER BY ID"
    )
    List<LandData> getLands(long snapshot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandData land);

    @Delete
    void delete(LandData land);
}
