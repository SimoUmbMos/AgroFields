package com.mosc.simo.ptuxiaki3741.backend.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.backend.dao.CalendarNotificationDao;
import com.mosc.simo.ptuxiaki3741.backend.dao.LandZoneHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.dao.SnapshotDao;
import com.mosc.simo.ptuxiaki3741.backend.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.backend.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.dao.LandZoneDao;
import com.mosc.simo.ptuxiaki3741.backend.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.entities.Snapshot;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.typeconverters.DBTypesConverter;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

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
