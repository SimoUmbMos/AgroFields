package com.mosc.simo.ptuxiaki3741.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.database.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandPointDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandPointHistoryDao;
import com.mosc.simo.ptuxiaki3741.database.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.models.LandData;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.database.typeconverters.DBTypesConverter;

@Database(entities = {
        User.class,
        LandData.class,
        LandPoint.class,
        LandRecord.class,
        LandPointRecord.class
}, version = AppDatabase.DATABASE_VERSION)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class AppDatabase extends RoomDatabase {
    public static final int DATABASE_VERSION = 2;
    public abstract UserDao userDao();
    public abstract LandDao landDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract LandPointDao landPointDao();
    public abstract LandPointHistoryDao landPointHistoryDao();
}
