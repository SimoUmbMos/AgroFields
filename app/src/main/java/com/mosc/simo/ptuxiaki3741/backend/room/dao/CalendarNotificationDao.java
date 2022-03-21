package com.mosc.simo.ptuxiaki3741.backend.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;

import java.util.List;


@Dao
public interface CalendarNotificationDao {
    @Query("Select * From CalendarNotification " +
            "ORDER BY CalendarNotification.Date ASC")
    List<CalendarNotification> getNotifications();

    @Query("Select * From CalendarNotification " +
            "Where CalendarNotification.ID = :id")
    CalendarNotification getNotificationById(long id);

    @Query("Select * From CalendarNotification " +
            "Where CalendarNotification.CategoryID = :categoryID")
    List<CalendarNotification> getNotificationsByCategory(long categoryID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CalendarNotification notification);

    @Delete
    void delete(CalendarNotification notification);
}
