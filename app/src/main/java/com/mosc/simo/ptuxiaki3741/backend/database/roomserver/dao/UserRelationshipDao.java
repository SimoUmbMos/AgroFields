package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

import java.util.List;

@Dao
public interface UserRelationshipDao {
    @Query("SELECT * FROM `UserRelationships`")
    List<UserRelationship> getAll();

    @Query("SELECT * FROM `UserRelationships` WHERE " +
    "(`SenderID` = :id1 AND `ReceiverID` = :id2) OR (`SenderID` = :id2 AND `ReceiverID` = :id1)")
    List<UserRelationship> getByIDs(long id1, long id2);

    @Query("SELECT * FROM `UserRelationships` WHERE `SenderID` = :id OR `ReceiverID` = :id")
    List<UserRelationship> getByUserID(long id);

    @Query("SELECT * FROM `UserRelationships` WHERE (`SenderID` = :id OR `ReceiverID` = :id) AND Type = :type")
    List<UserRelationship> getByUserIDAndType(long id, UserDBAction type);

    @Query("SELECT * FROM `UserRelationships` WHERE `SenderID` = :id AND Type = :type")
    List<UserRelationship> getBySenderIDAndType(long id, UserDBAction type);

    @Query("SELECT * FROM `UserRelationships` WHERE `ReceiverID` = :id AND Type = :type")
    List<UserRelationship> getByReceiverIDAndType(long id, UserDBAction type);

    @Delete
    void delete(UserRelationship userRelationship);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserRelationship userRelationship);

}
