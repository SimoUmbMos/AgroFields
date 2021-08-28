package com.mosc.simo.ptuxiaki3741.backend.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.backend.database.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.LandPointDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.LandPointHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.UserRelationshipDao;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.backend.database.typeconverters.DBTypesConverter;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

@Database(entities = {
        User.class,
        UserRelationship.class,
        LandData.class,
        LandPoint.class,
        LandDataRecord.class,
        LandPointRecord.class
}, version = RoomDatabase.DATABASE_VERSION)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public static final int DATABASE_VERSION = 20;
    public abstract UserDao userDao();
    public abstract LandDao landDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract LandPointDao landPointDao();
    public abstract LandPointHistoryDao landPointHistoryDao();
    public abstract UserRelationshipDao userRelationshipDao();
}