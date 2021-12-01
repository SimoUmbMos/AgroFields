package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.Contact;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM Contacts")
    List<Contact> getAllUsers();

    @Query("SELECT * FROM Contacts " +
            "WHERE Username GLOB  '*' || :search || '*' " +
            "LIMIT :limit OFFSET :offset")
    List<Contact> searchUserByUserName(String search, int limit, int offset);
    @Query("SELECT id FROM Contacts " +
            "WHERE Username GLOB  '*' || :search || '*' ")
    List<Long> searchUserByUserNamePage(String search);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Contact contact);

    @Delete
    void delete(Contact contact);
}
