package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomValues;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

import java.util.List;

@Dao
public interface UserRelationshipDao {
    @Query(RoomValues.UserRelationshipDaoValues.GetByIDs)
    List<UserRelationship> getByIDs(long id1, long id2);

    @Query(RoomValues.UserRelationshipDaoValues.GetByIDAndType)
    List<UserRelationship> getByIDAndType(long id, UserDBAction type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserRelationship userRelationship);

    @Delete
    void deleteAll(List<UserRelationship> userRelationships);
    @Query(RoomValues.UserRelationshipDaoValues.DeleteByUserID)
    void deleteByUserID(long uid);
    @Query(RoomValues.UserRelationshipDaoValues.DeleteByIDsAndType)
    void deleteByIDsAndType(long rid, long sid, UserDBAction type);

}
