package com.mosc.simo.ptuxiaki3741.ui.activities;

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

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityMainBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private NavHostFragment navHostFragment;
    private NotificationManager notificationManager;
    private FragmentBackPress fragmentBackPress;
    private boolean overrideDoubleBack = false, doubleBackToExit = false;

    public static RoomDatabase getRoomDb(Context context){
        return Room.databaseBuilder(context, RoomDatabase.class, "Main_db")
                .fallbackToDestructiveMigration().build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nhfMainNav);
    }

    @Override
    public void onBackPressed() {
        if(fragmentBackPress != null){
            if(!fragmentBackPress.onBackPressed()){
                return;
            }
        }
        if(overrideDoubleBack || doubleBackToExit){
            super.onBackPressed();
            return;
        }
        FragmentManager fm = navHostFragment.getChildFragmentManager();
        if(fm.getBackStackEntryCount() > 0){
            navHostFragment.getNavController().popBackStack();
        }else{
            doubleBackToExit = true;
            new Handler().postDelayed(() -> doubleBackToExit=false, AppValues.doubleTapBack);
            showToast(getResources().getText(R.string.double_tap_exit));
        }
    }

    private void init() {
        overrideDoubleBack = false;
        doubleBackToExit = false;
        fragmentBackPress = () -> true;
        checkThemeSettings();
        initNotificationChannel();
    }

    public void initNotificationChannel() {
        CharSequence name1 = getString(R.string.notification_channel_name);
        String description1 = getString(R.string.notification_channel_description);

        NotificationChannel channel1 = new NotificationChannel(
                AppValues.NotificationChannelID,
                name1,
                NotificationManager.IMPORTANCE_LOW
        );
        channel1.setDescription(description1);
        channel1.enableVibration(true);

        CharSequence name2 = getString(R.string.calendar_notification_channel_name);
        String description2 = getString(R.string.calendar_notification_channel_description);
        NotificationChannel channel2 = new NotificationChannel(
                AppValues.CalendarNotificationChannelID,
                name2,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel2.setDescription(description2);

        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel1);
        notificationManager.createNotificationChannel(channel2);
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
        Toast.makeText(this,text.toString(),Toast.LENGTH_SHORT).show();
    }

    public void setOverrideDoubleBack(boolean overrideDoubleBack){
        this.overrideDoubleBack = overrideDoubleBack;
    }

    public void setOnBackPressed(FragmentBackPress fragmentBackPress){
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