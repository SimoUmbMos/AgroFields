package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomValues;
import com.mosc.simo.ptuxiaki3741.models.entities.SharedLand;

import java.util.List;

@Dao
public interface SharedLandDao {

    @Query(RoomValues.SharedLandDaoValues.GetSharedLandsByUidAndLid)
    List<SharedLand> getSharedLandsByUidAndLid(long uid, long lid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SharedLand sharedLand);

    @Delete
    void delete(SharedLand sharedLand);
    @Delete
    void deleteAll(List<SharedLand> sharedLand);
    @Query(RoomValues.SharedLandDaoValues.DeleteByUserID)
    void deleteByUserID(long uid);
    @Query(RoomValues.SharedLandDaoValues.DeleteByLandID)
    void deleteByLandID(long lid);
}
