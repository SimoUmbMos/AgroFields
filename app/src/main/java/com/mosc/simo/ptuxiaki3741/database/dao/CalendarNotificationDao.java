package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;

import java.util.List;


@Dao
public interface CalendarNotificationDao {
    @Query("Select * From CalendarNotification ORDER BY Date ASC")
    List<CalendarNotification> getNotifications();

    @Query("Select * From CalendarNotification Where id = :id")
    CalendarNotification getNotificationsById(long id);

    @Query("Select * From CalendarNotification Where LandID = :lid")
    List<CalendarNotification> getNotificationsByLid(long lid);

    @Query("Select * From CalendarNotification Where ZoneID = :zid")
    List<CalendarNotification> getNotificationsByZid(long zid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CalendarNotification notification);

    @Delete
    void delete(CalendarNotification notification);
}
