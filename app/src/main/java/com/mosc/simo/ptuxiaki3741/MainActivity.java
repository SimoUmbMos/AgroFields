package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

public class MainActivity extends AppCompatActivity {
    private static final int doubleTapBack = 2750;
    private FragmentBackPress fragmentBackPress;
    private NavHostFragment navHostFragment;
    private boolean doubleBackToExitPressedOnce = false;

    public static AppDatabase getDb(Context context){
        return Room.databaseBuilder(context,
                AppDatabase.class, "Main_db").fallbackToDestructiveMigration().build();
    }

    public void showSnackBar(CharSequence text) {
        showSnackBar(text.toString());
    }
    public void showSnackBar(String text) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_root), text, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.colorPrimary, getTheme()));
        View view = snackbar.getView();
        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

    private void initViewModels() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userViewModel.init(sharedPref);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        initViewModels();
        init();
    }
    @Override
    public void onBackPressed() {
        if(fragmentBackPress.onBackPressed()){
            if(navHostFragment.getChildFragmentManager().getBackStackEntryCount() == 0){
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                showSnackBar(getResources().getText(R.string.double_tap_exit));
                doubleBackToExitPressedOnce = true;
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, doubleTapBack);
            }else{
                super.onBackPressed();
            }
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