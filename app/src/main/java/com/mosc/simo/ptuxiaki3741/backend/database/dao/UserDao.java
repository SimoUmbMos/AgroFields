package com.mosc.simo.ptuxiaki3741.backend.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomValues;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query(RoomValues.UserDaoValues.SearchUserByUserName)
    List<User> searchUserByUserName(long searcherID, String search);

    @Query(RoomValues.UserDaoValues.GetUsersByReceiverIDAndType)
    List<User> getUsersByReceiverIDAndType(long receiverID, UserDBAction type);

    @Query(RoomValues.UserDaoValues.GetUsersBySenderIDAndType)
    List<User> getUsersBySenderIDAndType(long receiverID, UserDBAction type);

    @Query(RoomValues.UserDaoValues.GetUserById)
    User getUserById(long id);

    @Query(RoomValues.UserDaoValues.GetUserByUserName)
    User getUserByUserName(String username);

    @Query(RoomValues.UserDaoValues.GetUserByUserNameAndPassword)
    User getUserByUserNameAndPassword(String username,String password);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Delete
    void delete(User user);

}
