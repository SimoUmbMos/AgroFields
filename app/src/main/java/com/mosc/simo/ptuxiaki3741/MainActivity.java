package com.mosc.simo.ptuxiaki3741;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mosc.simo.ptuxiaki3741.backend.database.restserver.RestDatabase;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import static com.mosc.simo.ptuxiaki3741.util.UIUtil.getColorOnPrimaryFromTheme;


public class MainActivity extends AppCompatActivity {
    private static final int doubleTapBack = 2750;
    private static final String TAG = "MainActivity";
    private FragmentBackPress fragmentBackPress;
    private NavHostFragment navHostFragment;
    private boolean doubleBackToExitPressedOnce = false;

    public static RoomDatabase getRoomDb(Context context){
        return Room.databaseBuilder(context,
                RoomDatabase.class, "Main_db").fallbackToDestructiveMigration().build();
    }

    public static RestDatabase getRestDb(){
        return new RestDatabase();
    }

    public void showToast(CharSequence text) {
        showToast(text.toString());
    }
    public void showToast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    private void init() {
        fragmentBackPress = new FragmentBackPress(){
            @Override
            public boolean onBackPressed() {
                return true;
            }
        };
        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setTitleTextColor(getColorOnPrimaryFromTheme(getApplicationContext()));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setTitle(null);
        }
    }

    private void initViewModels() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userViewModel.init(sharedPref);
        userViewModel.getCurrUser().observe(this,this::onUserUpdate);
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
        this.fragmentBackPress = fragmentBackPress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        init();
        initViewModels();
    }
    @Override
    public void onBackPressed() {
        if(fragmentBackPress.onBackPressed()){
            if(navHostFragment.getChildFragmentManager().getBackStackEntryCount() == 0){
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                showToast(getResources().getText(R.string.double_tap_exit));
                doubleBackToExitPressedOnce = true;
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, doubleTapBack);
            }else{
                super.onBackPressed();
            }
        }
    }
}