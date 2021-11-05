package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

import java.util.List;

@Dao
public interface UserRelationshipDao {
    @Query("SELECT * FROM UserRelationships " +
            "WHERE (SenderID = :id1 AND ReceiverID = :id2) " +
            "OR (SenderID = :id2 AND ReceiverID = :id1)")
    List<UserRelationship> getByIDs(long id1, long id2);

    @Query("SELECT * FROM UserRelationships " +
            "WHERE (ReceiverID = :id OR SenderID = :id) AND Type = :type")
    List<UserRelationship> getByIDAndType(long id, UserDBAction type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserRelationship userRelationship);

    @Delete
    void deleteAll(List<UserRelationship> userRelationships);
    @Query("DELETE FROM UserRelationships " +
            "WHERE SenderID = :uid " +
            "OR ReceiverID = :uid")
    void deleteByUserID(long uid);
    @Query("DELETE FROM UserRelationships " +
            "WHERE ReceiverID = :rid " +
            "AND SenderID = :sid " +
            "AND Type = :type")
    int deleteByIDsAndType(long rid, long sid, UserDBAction type);

}
