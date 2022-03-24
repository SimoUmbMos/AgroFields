package com.mosc.simo.ptuxiaki3741.ui.applications;

import android.app.Application;

import androidx.room.Room;

import com.mosc.simo.ptuxiaki3741.backend.room.database.RoomDatabase;

public class MainApplication extends Application {

    public static RoomDatabase getRoomDb(Application application){
        androidx.room.RoomDatabase.Builder<RoomDatabase> builder = Room.databaseBuilder(
                application,
                RoomDatabase.class,
                "Main_db"
        );
        return builder.build();
    }

}
