package com.mosc.simo.ptuxiaki3741;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityMainBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FragmentBackPress fragmentBackPress;
    private NavHostFragment navHostFragment;
    private boolean doubleBackToExitPressedOnce = false;
    private ActivityMainBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        initViewModels();
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fcvNavHostFragment);
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override public void onBackPressed() {
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
        return Room.databaseBuilder(context,
                RoomDatabase.class, "Main_db").fallbackToDestructiveMigration().build();
    }

    private void init() {
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
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userViewModel.init(sharedPref);
        userViewModel.getCurrUser().observe(this,this::onUserUpdate);
    }

    public void showToast(CharSequence text) {
        showToast(text.toString());
    }
    public void showToast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    private void onUserUpdate(User user) {
        if(user != null){
            Log.d(TAG, "onUserUpdate: user not null");
        }else{
            Log.d(TAG, "onUserUpdate: user null");
        }
        LandViewModel landViewModel = new ViewModelProvider(this).get(LandViewModel.class);
        landViewModel.init(user);
    }

    public void setOnBackPressed(FragmentBackPress fragmentBackPress){
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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