package com.mosc.simo.ptuxiaki3741;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.os.Bundle;

import com.mosc.simo.ptuxiaki3741.database.AppDatabase;

public class MainActivity extends AppCompatActivity {

    public static AppDatabase getDb(Context context){
        return Room.databaseBuilder(context,
                AppDatabase.class, "Main_db").fallbackToDestructiveMigration().build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}