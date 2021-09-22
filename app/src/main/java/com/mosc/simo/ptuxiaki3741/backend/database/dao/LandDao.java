package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomValues;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import java.util.List;

@Dao
public interface LandDao {
    @Query(RoomValues.LandDaoValues.GetLands)
    List<LandData> getLands();

    @Query(RoomValues.LandDaoValues.GetLandData)
    LandData getLandData(long lid);

    @Query(RoomValues.LandDaoValues.GetLandByCreatorId)
    List<LandData> getLandByCreatorId(long uid);

    @Query(RoomValues.LandDaoValues.GetUserSharedLands)
    List<LandData> getUserSharedLands(long uid);

    @Query(RoomValues.LandDaoValues.GetSharedLandsToUser)
    List<LandData> getSharedLandsToUser(long ownerID, long sharedId);

    @Query(RoomValues.LandDaoValues.GetSharedLandsToOtherUsers)
    List<LandData> getSharedLandsToOtherUsers(long ownerID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandData land);

    @Delete
    void delete(LandData land);
    @Query(RoomValues.LandDaoValues.DeleteByUID)
    void deleteByUID(long uid);
}
