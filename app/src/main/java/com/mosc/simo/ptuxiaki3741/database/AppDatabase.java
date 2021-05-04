package com.mosc.simo.ptuxiaki3741.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.database.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandOwnerDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandPointDao;
import com.mosc.simo.ptuxiaki3741.database.dao.UserDao;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandOwner;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.database.model.User;
import com.mosc.simo.ptuxiaki3741.database.typeconverters.DateConverter;

@Database(entities = {User.class, Land.class, LandPoint.class, LandOwner.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract LandDao landDao();
    public abstract LandOwnerDao landOwnerDao();
    public abstract LandPointDao landPointDao();
}
