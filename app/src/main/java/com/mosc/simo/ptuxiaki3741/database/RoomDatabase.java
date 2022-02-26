package com.mosc.simo.ptuxiaki3741.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.database.dao.CalendarNotificationDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandZoneHistoryDao;
import com.mosc.simo.ptuxiaki3741.database.dao.SnapshotDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandZoneDao;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.Snapshot;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.database.typeconverters.DBTypesConverter;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

@Database(entities = {
        LandData.class,
        LandZoneData.class,
        Snapshot.class,
        LandDataRecord.class,
        LandZoneDataRecord.class,
        CalendarNotification.class
}, version = AppValues.DATABASE_VERSION)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public abstract SnapshotDao snapshotDao();
    public abstract LandDao landDao();
    public abstract LandZoneDao landZoneDao();
    public abstract LandZoneHistoryDao landZoneHistoryDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract CalendarNotificationDao calendarNotificationDao();
}
