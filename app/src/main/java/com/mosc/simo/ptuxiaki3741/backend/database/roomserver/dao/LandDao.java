package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.LandData;

import java.util.List;

@Dao
public interface LandDao {
    @Query("SELECT * FROM LandData")
    List<LandData> getLands();

    @Query("SELECT * FROM LandData WHERE `id` = :lid")
    LandData getLandData(long lid);

    @Query("SELECT * FROM LandData WHERE `CreatorId` = :uid")
    List<LandData> getLandByCreatorId(long uid);

    @Query("DELETE FROM LandData WHERE `CreatorId` = :uid")
    void deleteByUID(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandData land);

    @Delete
    void delete(LandData land);
}
