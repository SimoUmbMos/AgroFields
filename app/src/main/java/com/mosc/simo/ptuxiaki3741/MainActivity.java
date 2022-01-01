package com.mosc.simo.ptuxiaki3741;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavHostFragment navHostFragment;
    private NotificationManager notificationManager;
    private FragmentBackPress fragmentBackPress;
    private boolean overrideDoubleBack = false,
            doubleBackToExit = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nhfMainNav);
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override public void onBackPressed() {
        if(overrideDoubleBack){
            super.onBackPressed();
            return;
        }
        if(fragmentBackPress.onBackPressed()){
            FragmentManager fm = navHostFragment.getChildFragmentManager();
            if(fm.getBackStackEntryCount() > 0){
                navHostFragment.getNavController().popBackStack();
            }else{
                if (doubleBackToExit) {
                    super.onBackPressed();
                    return;
                }
                doubleBackToExit = true;
                new Handler().postDelayed(() -> doubleBackToExit=false, AppValues.doubleTapBack);
                showToast(getResources().getText(R.string.double_tap_exit));
            }
        }
    }

    public static RoomDatabase getRoomDb(Context context){
        return Room.databaseBuilder(context, RoomDatabase.class, "Main_db")
                .fallbackToDestructiveMigration().build();
    }

    private void init() {
        overrideDoubleBack = false;
        fragmentBackPress = () -> true;
        setSupportActionBar(binding.tbMainActivity);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        checkThemeSettings();
        initNotificationChannel();
    }
    public void initNotificationChannel() {
        CharSequence name = getString(R.string.notification_channel_name);
        String description = getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(AppValues.NotificationChannelID, name, importance);
        channel.enableVibration(true);
        channel.setDescription(description);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public Intent getIntentIfCalledByFile(){
        if(getIntent() != null) {
            if (getIntent().getData() != null) {
                return getIntent();
            }
        }
        return null;
    }
    public NotificationManager getNotificationManager(){
        if(notificationManager == null) initNotificationChannel();
        return notificationManager;
    }

    public void showToast(CharSequence text) {
        showToast(text.toString());
    }
    public void showToast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    public void setOverrideDoubleBack(boolean overrideDoubleBack){
        this.overrideDoubleBack = overrideDoubleBack;
    }
    public void setOnBackPressed(FragmentBackPress fragmentBackPress){
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overrideDoubleBack = false;
        this.fragmentBackPress = fragmentBackPress;
    }

    public void checkThemeSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.getBoolean(AppValues.isForceKey, false)){
            if(sharedPref.getBoolean(AppValues.isDarkKey, false)){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}