package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import java.util.List;

@Dao
public interface LandDao {
    @Query("SELECT * FROM LandData")
    List<LandData> getLands();

    @Query("SELECT * FROM LandData WHERE `id` = :lid")
    LandData getLandData(long lid);

    @Query("SELECT * FROM LandData WHERE `CreatorID` = :uid")
    List<LandData> getLandByCreatorId(long uid);

    @Query("SELECT l.* FROM LandData l " +
            "INNER JOIN SharedLands s ON l.id = s.LandID " +
            "WHERE s.UserID = :uid")
    List<LandData> getUserSharedLands(long uid);

    @Query("SELECT l.* FROM LandData l " +
            "INNER JOIN SharedLands s ON l.id = s.LandID " +
            "WHERE l.CreatorID = :ownerID " +
            "AND s.UserID = :sharedId ")
    List<LandData> getSharedLandsToUser(long ownerID, long sharedId);

    @Query("SELECT l.* FROM LandData l " +
            "INNER JOIN SharedLands s ON l.id = s.LandID " +
            "WHERE l.CreatorID = :ownerID ")
    List<LandData> getSharedLandsToOtherUsers(long ownerID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandData land);

    @Delete
    void delete(LandData land);
    @Query("DELETE FROM LandData WHERE `CreatorID` = :uid")
    void deleteByUID(long uid);
}
