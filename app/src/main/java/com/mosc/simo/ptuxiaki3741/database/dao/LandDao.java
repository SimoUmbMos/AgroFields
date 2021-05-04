package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.database.model.Land;

import java.util.List;

@Dao
public interface LandDao {
    @Query("SELECT * FROM `Lands` Where `id` = :id")
    Land getLandById(long id);

    @Query("SELECT * FROM `Lands` WHERE `id` IN (:ids)")
    List<Land> getLandsByIds(List<Long> ids);

    @Query("SELECT * FROM `Lands` WHERE `CreatorId` = :uid")
    List<Land> getLandByCreatorId(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Land land);

    @Delete
    int delete(Land land);
}
