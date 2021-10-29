package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;

import java.util.List;

@Dao
public interface UserLandPermissionsDao {
    @Query("SELECT * FROM UserLandPermissions " +
            "WHERE LandID = :lid " +
            "AND UserID = :uid")
    List<UserLandPermissions> getSharedLandsByUidAndLid(long uid, long lid);

    @Query("SELECT UserLandPermissions.* " +
            "FROM LandData INNER JOIN UserLandPermissions ON LandData.id = UserLandPermissions.LandID " +
            "WHERE LandData.CreatorID = :uid1 " +
            "AND UserLandPermissions.UserID = :uid2")
    List<UserLandPermissions> getSharedLandsByCreatorIDAndUid(long uid1, long uid2);

    @Query("SELECT LandData.* , UserLandPermissions.* " +
            "FROM LandData INNER JOIN UserLandPermissions ON LandData.id = UserLandPermissions.LandID " +
            "WHERE LandData.CreatorID = :ownerID " +
            "AND UserLandPermissions.UserID = :sharedId")
    List<Land> getSharedLandsToUser(long ownerID, long sharedId);

    @Query("SELECT LandData.* , UserLandPermissions.* " +
            "FROM LandData INNER JOIN UserLandPermissions ON LandData.id = UserLandPermissions.LandID " +
            "WHERE LandData.CreatorID = :ownerID")
    List<Land> getSharedLandsToOtherUsers(long ownerID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserLandPermissions userLandPermissions);

    @Delete
    void deleteAll(List<UserLandPermissions> userLandPermissions);
    @Query("DELETE FROM UserLandPermissions " +
            "WHERE UserID = :uid")
    void deleteByUserID(long uid);
    @Query("DELETE FROM UserLandPermissions " +
            "WHERE LandID = :lid")
    void deleteByLandID(long lid);
}
