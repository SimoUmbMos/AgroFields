package com.mosc.simo.ptuxiaki3741.backend.room.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.backend.room.dao.CalendarCategoriesDao;
import com.mosc.simo.ptuxiaki3741.backend.room.dao.CalendarNotificationDao;
import com.mosc.simo.ptuxiaki3741.backend.room.dao.LandZoneHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.room.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.backend.room.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.room.dao.LandZoneDao;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.room.typeconverters.DBTypesConverter;

@Database(
        version = 1,
        entities = {
            LandData.class,
            LandZoneData.class,
            LandDataRecord.class,
            LandZoneDataRecord.class,
            CalendarCategory.class,
            CalendarNotification.class
        }
)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public abstract LandDao landDao();
    public abstract LandZoneDao landZoneDao();
    public abstract LandZoneHistoryDao landZoneHistoryDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract CalendarNotificationDao calendarNotificationDao();
    public abstract CalendarCategoriesDao calendarCategoriesDao();
}
