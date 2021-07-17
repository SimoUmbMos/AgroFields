package com.mosc.simo.ptuxiaki3741.backend.database.roomserver;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandPointDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandPointHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.models.LandData;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.models.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.typeconverters.DBTypesConverter;

@Database(entities = {
        User.class,
        LandData.class,
        LandPoint.class,
        LandDataRecord.class,
        LandPointRecord.class
}, version = RoomDatabase.DATABASE_VERSION)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public static final int DATABASE_VERSION = 6;
    public abstract UserDao userDao();
    public abstract LandDao landDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract LandPointDao landPointDao();
    public abstract LandPointHistoryDao landPointHistoryDao();
}
