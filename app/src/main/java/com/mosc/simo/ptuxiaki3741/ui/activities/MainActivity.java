package com.mosc.simo.ptuxiaki3741.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityMainBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.ui.dialogs.LoadingDialog;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavHostFragment navHostFragment;
    private FragmentBackPress fragmentBackPress;
    private boolean overrideDoubleBack = false, doubleBackToExit = false;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nhfMainNav);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
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
            showSnackBar(getResources().getText(R.string.double_tap_exit));
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
        CharSequence name = getString(R.string.calendar_notification_channel_name);
        String description = getString(R.string.calendar_notification_channel_description);
        NotificationChannel channel = new NotificationChannel(
                AppValues.CalendarNotificationChannelID,
                name,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
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

    public void showSnackBar(CharSequence text) {
        Snackbar snackbar = Snackbar.make(binding.clSnackBarContainer,text,Snackbar.LENGTH_LONG);
        snackbar.show();
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

    public LoadingDialog getLoadingDialog() {
        if(loadingDialog == null) loadingDialog = new LoadingDialog(this);
        return loadingDialog;
    }
}