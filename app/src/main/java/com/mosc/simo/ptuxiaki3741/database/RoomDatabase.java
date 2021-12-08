package com.mosc.simo.ptuxiaki3741.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.mosc.simo.ptuxiaki3741.database.dao.LandDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandHistoryDao;
import com.mosc.simo.ptuxiaki3741.database.dao.LandZoneDao;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.database.typeconverters.DBTypesConverter;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

@Database(entities = {
        LandData.class,
        LandZoneData.class,
        LandDataRecord.class
}, version = AppValues.DATABASE_VERSION)
@TypeConverters({
        DBTypesConverter.class
})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public abstract LandDao landDao();
    public abstract LandZoneDao landZoneDao();
    public abstract LandHistoryDao landHistoryDao();
}
