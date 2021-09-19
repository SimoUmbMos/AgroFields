package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.SharedLand;

import java.util.List;

@Dao
public interface SharedLandDao {

    @Query("SELECT s.* FROM SharedLands s " +
            "WHERE s.LandID = :lid " +
            "AND s.UserID = :uid")
    List<SharedLand> getSharedLandsByUidAndLid(long uid, long lid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SharedLand sharedLand);

    @Delete
    void delete(SharedLand sharedLand);
    @Delete
    void deleteAll(List<SharedLand> sharedLand);
    @Query("DELETE FROM SharedLands WHERE UserID = :uid")
    void deleteByUserID(long uid);

}
