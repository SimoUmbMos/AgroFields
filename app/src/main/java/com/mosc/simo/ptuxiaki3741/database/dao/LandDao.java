package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import java.util.List;

@Dao
public interface LandDao {
    @Query("SELECT * FROM LandData " +
            "WHERE id = :lid")
    LandData getLandData(long lid);

    @Query("SELECT LandData.*, perm.* " +
            "FROM LandData INNER JOIN UserLandPermissions perm ON LandData.id = perm.LandID " +
            "WHERE perm.UserID = :uid")
    List<Land> getUserSharedLands(long uid);

    @Query("SELECT * FROM LandData " +
            "WHERE CreatorID = :uid")
    List<LandData> getLandByCreatorId(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandData land);

    @Delete
    void delete(LandData land);
    @Query("DELETE FROM LandData " +
            "WHERE CreatorID = :uid")
    void deleteByUID(long uid);
}
