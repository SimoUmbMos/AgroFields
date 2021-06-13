package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.database.model.User;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;

public class MainActivity extends AppCompatActivity {
    private FragmentBackPress fragmentBackPress;
    private User user = null;

    public static AppDatabase getDb(Context context){
        return Room.databaseBuilder(context,
                AppDatabase.class, "Main_db").fallbackToDestructiveMigration().build();
    }

    private void init() {
        fragmentBackPress = new FragmentBackPress(){
            @Override
            public boolean onBackPressed() {
                return true;
            }
        };
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setTitle(null);
        }
    }

    public void setOnBackPressed(FragmentBackPress fragmentBackPress){
        this.fragmentBackPress = fragmentBackPress;
    }

    public User getUser() {
        if(user != null)
            return user;
        else
            return getUserFromMemory();
    }

    public void setUser(User user) {
        storeUser(user);
        this.user = user;
    }
    public void setUser(long userID) {
        storeUser(userID);
        if (userID != -1) {
            AppDatabase db = getDb(this);
            user = db.userDao().getUserById(userID);
            db.close();
        }else{
            user = null;
        }
    }

    private void storeUser(User user){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("currUser", user.getId());
        editor.apply();
    }
    private void storeUser(long userID){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("currUser", userID);
        editor.apply();
    }
    private User getUserFromMemory() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        long userID = sharedPref.getLong("currUser", -1);
        if (userID != -1){
            AppDatabase db = getDb(this);
            user = db.userDao().getUserById(userID);
            db.close();
            return user;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    @Override
    public void onBackPressed() {
        if(fragmentBackPress.onBackPressed()){
            super.onBackPressed();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}