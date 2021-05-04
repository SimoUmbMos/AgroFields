package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.database.model.LandOwner;

import java.util.List;

@Dao
public interface LandOwnerDao {
    @Query("SELECT * FROM `LandOwners` WHERE `id` = :id")
    LandOwner getLandOwnerById(long id);

    @Query("SELECT * FROM `LandOwners` WHERE `lid` = :lid")
    List<LandOwner> getAllOwnersOfLand(long lid);

    @Query("SELECT * FROM `LandOwners` WHERE `Uid` = :uid")
    List<LandOwner> getAllLandsOfOwner(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandOwner landOwner);

    @Delete
    int delete(LandOwner landOwner);

    @Delete
    int deleteAll(List<LandOwner> landOwners);
}
