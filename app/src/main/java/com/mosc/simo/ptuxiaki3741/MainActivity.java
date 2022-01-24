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
import android.view.View;
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
        setSupportActionBar(binding.toolbar);
        binding.toolbarTitle.setText(binding.toolbar.getTitle());
        binding.toolbarSubTitle.setText(binding.toolbar.getSubtitle());
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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
            setToolbarElevation(4);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overrideDoubleBack = false;
        this.fragmentBackPress = fragmentBackPress;
    }
    public void setToolbarElevation(int elevation){
        binding.ablMainActivity.setElevation(elevation);
    }
    public void setToolbarTitle(String title){
        if(title != null){
            binding.toolbarTitle.setText(title);
            binding.toolbarTitle.setVisibility(View.VISIBLE);
        }else{
            binding.toolbarTitle.setText("");
            binding.toolbarTitle.setVisibility(View.GONE);
        }
        binding.toolbarSubTitle.setText("");
        binding.toolbarSubTitle.setVisibility(View.GONE);
    }
    public void setToolbarTitle(String title, String subTitle) {
        if(title != null){
            binding.toolbarTitle.setText(title);
            binding.toolbarTitle.setVisibility(View.VISIBLE);
        }else{
            binding.toolbarTitle.setText("");
            binding.toolbarTitle.setVisibility(View.GONE);
        }
        if(subTitle != null){
            binding.toolbarSubTitle.setText(subTitle);
            binding.toolbarSubTitle.setVisibility(View.VISIBLE);
        }else{
            binding.toolbarSubTitle.setText("");
            binding.toolbarSubTitle.setVisibility(View.GONE);
        }
    }
    public String getToolbarTitle(){
        if(binding.toolbarTitle.getText() != null){
            return binding.toolbarTitle.getText().toString();
        }
        return "";
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