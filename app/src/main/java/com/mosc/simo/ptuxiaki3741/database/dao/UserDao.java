package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM Users " +
            "WHERE Username GLOB  '*' || :search || '*' " +
            "AND id != :searcherID")
    List<User> searchUserByUserName(long searcherID, String search);

    @Query("SELECT u.* FROM Users u INNER JOIN UserRelationships ur ON u.id = ur.SenderID " +
            "WHERE ur.ReceiverID = :receiverID " +
            "AND ur.Type = :type")
    List<User> getUsersByReceiverIDAndType(long receiverID, UserDBAction type);

    @Query("SELECT u.* FROM Users u INNER JOIN UserRelationships ur ON u.id = ur.ReceiverID " +
            "WHERE ur.SenderID = :receiverID " +
            "AND ur.Type = :type")
    List<User> getUsersBySenderIDAndType(long receiverID, UserDBAction type);

    @Query("SELECT * FROM Users " +
            "WHERE id = :id")
    User getUserById(long id);

    @Query("SELECT * FROM Users " +
            "WHERE Username = :username")
    User getUserByUserName(String username);

    @Query("SELECT * FROM Users " +
            "WHERE Username = :username " +
            "AND Password = :password")
    User getUserByUserNameAndPassword(String username,String password);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    void delete(User user);

}
