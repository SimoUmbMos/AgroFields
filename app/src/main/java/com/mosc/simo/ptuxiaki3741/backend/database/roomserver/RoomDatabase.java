package com.mosc.simo.ptuxiaki3741.backend.database.roomserver;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandPointDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.LandPointHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.dao.UserRelationshipDao;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.typeconverters.DBTypesConverter;
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
    public static final int DATABASE_VERSION = 15;
    public abstract UserDao userDao();
    public abstract LandDao landDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract LandPointDao landPointDao();
    public abstract LandPointHistoryDao landPointHistoryDao();
    public abstract UserRelationshipDao userRelationshipDao();
}
