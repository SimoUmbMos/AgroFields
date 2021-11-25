package com.mosc.simo.ptuxiaki3741;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class MainActivity extends AppCompatActivity{
    public static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavHostFragment navHostFragment;

    private FragmentBackPress fragmentBackPress;

    private UserViewModel userViewModel;
    private LandViewModel landViewModel;
    private boolean overrideDoubleBack = false,
            doubleBackToExitPressedOnce = false,
            doRefresh = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        initViewModels();
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nhfMainNav);
        initBackgroundLoop();
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
            if(navHostFragment.getChildFragmentManager().getBackStackEntryCount() == 0){
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                showToast(getResources().getText(R.string.double_tap_exit));
                doubleBackToExitPressedOnce = true;
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, AppValues.doubleTapBack);
            }else{
                super.onBackPressed();
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
    }
    private void initViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        landViewModel = new ViewModelProvider(this).get(LandViewModel.class);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        AsyncTask.execute(()-> userViewModel.init(sharedPref));
        userViewModel.getCurrUser().observe(this,this::onUserUpdate);
    }
    private void initBackgroundLoop() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    if(doRefresh)
                        refreshLists();
                } catch (Exception e) {
                    Log.e(TAG, "initBackgroundLoop: ", e);
                } finally {
                    handler.postDelayed(this,AppValues.backgroundInterval);
                }
            }
        };
        handler.postDelayed(runnable,AppValues.backgroundInterval);
    }

    private void refreshLists() {
        if(userViewModel != null){
            AsyncTask.execute(()->userViewModel.refreshVM());
        }
    }

    public Intent getIntentIfCalledByFile(){
        if(getIntent() != null) {
            if (getIntent().getData() != null) {
                return getIntent();
            }
        }
        return null;
    }

    public void showToast(CharSequence text) {
        showToast(text.toString());
    }
    public void showToast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    private void onUserUpdate(User user) {
        AsyncTask.execute(()-> landViewModel.init(user));
    }

    public void setOverrideDoubleBack(boolean overrideDoubleBack){
        this.overrideDoubleBack = overrideDoubleBack;
    }
    public void setDoRefresh(boolean doRefresh){
        this.doRefresh = doRefresh;
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