package com.mosc.simo.ptuxiaki3741.backend.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.backend.database.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.SharedLandDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.backend.database.dao.UserRelationshipDao;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.SharedLand;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.backend.database.typeconverters.DBTypesConverter;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

@Database(entities = {
        User.class,
        UserRelationship.class,
        LandData.class,
        SharedLand.class,
        LandDataRecord.class
}, version = RoomDatabase.DATABASE_VERSION)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public static final int DATABASE_VERSION = 22;
    public abstract UserDao userDao();
    public abstract LandDao landDao();
    public abstract LandHistoryDao landHistoryDao();
    public abstract UserRelationshipDao userRelationshipDao();
    public abstract SharedLandDao sharedLandDao();
}
