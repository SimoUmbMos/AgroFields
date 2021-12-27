package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.JunctionLandTag;
import com.mosc.simo.ptuxiaki3741.models.entities.JunctionZoneTag;
import com.mosc.simo.ptuxiaki3741.models.entities.TagData;

import java.util.List;

@Dao
public interface TagDao {

    @Query("SELECT * FROM TagData WHERE id = :id")
    TagData getTagByID(long id);

    @Query("SELECT * FROM TagData")
    List<TagData> getTags();

    @Query("SELECT t.* " +
            "FROM TagData t INNER JOIN JunctionLandTag j ON j.tag_id = t.id " +
            "WHERE j.land_id = :lid")
    List<TagData> getTagsByLID(long lid);

    @Query("SELECT t.* " +
            "FROM TagData t INNER JOIN JunctionZoneTag j ON j.tag_id = t.id " +
            "WHERE j.zone_id = :zid")
    List<TagData> getTagsByZID(long zid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TagData tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(JunctionLandTag tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(JunctionZoneTag tag);

    @Delete
    void delete(TagData tag);
}
