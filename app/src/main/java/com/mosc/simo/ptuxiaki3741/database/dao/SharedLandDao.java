package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.LandWithShare;
import com.mosc.simo.ptuxiaki3741.models.entities.SharedLand;

import java.util.List;

@Dao
public interface SharedLandDao {
    @Query("SELECT * FROM SharedLands " +
            "WHERE LandID = :lid " +
            "AND UserID = :uid")
    List<SharedLand> getSharedLandsByUidAndLid(long uid, long lid);

    @Query("SELECT SharedLands.* " +
            "FROM LandData INNER JOIN SharedLands ON LandData.id = SharedLands.LandID " +
            "WHERE LandData.CreatorID = :uid1 " +
            "AND SharedLands.UserID = :uid2")
    List<SharedLand> getSharedLandsByCreatorIDAndUid(long uid1, long uid2);

    @Query("SELECT LandData.* , SharedLands.* " +
            "FROM LandData INNER JOIN SharedLands ON LandData.id = SharedLands.LandID " +
            "WHERE LandData.CreatorID = :ownerID " +
            "AND SharedLands.UserID = :sharedId")
    List<LandWithShare> getSharedLandsToUser(long ownerID, long sharedId);

    @Query("SELECT LandData.* , SharedLands.* " +
            "FROM LandData INNER JOIN SharedLands ON LandData.id = SharedLands.LandID " +
            "WHERE LandData.CreatorID = :ownerID")
    List<LandWithShare> getSharedLandsToOtherUsers(long ownerID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SharedLand sharedLand);

    @Delete
    void deleteAll(List<SharedLand> sharedLand);
    @Query("DELETE FROM SharedLands " +
            "WHERE UserID = :uid")
    void deleteByUserID(long uid);
    @Query("DELETE FROM SharedLands " +
            "WHERE LandID = :lid")
    void deleteByLandID(long lid);
}
