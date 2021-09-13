package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM `Users` ")
    List<User> getUsers();

    @Query("SELECT * FROM Users " +
            "WHERE Username GLOB  '*' || :search || '*' AND id != :searcherID "
    )
    List<User> searchUserByUserName(long searcherID, String search);

    @Query("SELECT COUNT(*) FROM Users " +
            "WHERE Users.Username GLOB  '*' || :search || '*' AND Users.id != :searcherID "+

            "AND Users.id NOT IN (" +
            "SELECT UserRelationships.SenderID FROM UserRelationships " +
            "WHERE ReceiverID = :searcherID " +
            "AND Type = :type) " +

            "AND Users.id NOT IN (" +
            "SELECT UserRelationships.ReceiverID FROM UserRelationships " +
            "WHERE SenderID = :searcherID " +
            "AND Type = :type) "
    )
    int searchResultCount(long searcherID, String search, UserDBAction type);

    @Query("SELECT * FROM `Users` " +
            "WHERE `id` = :id")
    User getUserById(long id);

    @Query("SELECT * FROM `Users` " +
            "WHERE `Username` = :username")
    User getUserByUserName(String username);

    @Query("SELECT * FROM `Users` " +
            "WHERE `Username` = :username AND `Password` = :password")
    User getUserByUserNameAndPassword(String username,String password);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    void delete(User user);

}
