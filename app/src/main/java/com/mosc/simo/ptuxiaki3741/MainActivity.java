package com.mosc.simo.ptuxiaki3741;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.mosc.simo.ptuxiaki3741.backend.database.restserver.RestDatabase;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.models.User;

public class MainActivity extends AppCompatActivity {
    private static final int doubleTapBack = 2750;
    private static final String TAG = "MainActivity";
    private FragmentBackPress fragmentBackPress;
    private NavHostFragment navHostFragment;
    private boolean doubleBackToExitPressedOnce = false,
                        closeAfterImport = false;

    private final ActivityResultLauncher<Intent> fileImportResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::importResult
    );

    public static RoomDatabase getRoomDb(Context context){
        return Room.databaseBuilder(context,
                RoomDatabase.class, "Main_db").fallbackToDestructiveMigration().build();
    }

    public static RestDatabase getRestDb(){
        return new RestDatabase();
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

    private void initViewModels() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userViewModel.init(sharedPref);
        userViewModel.getCurrUser().observe(this,this::onUserUpdate);
        onUserUpdate(userViewModel.getCurrUser().getValue());
    }

    private void onUserUpdate(User user) {
        boolean userIsNull;
        if(user != null){
            userIsNull = false;
            Log.d(TAG, "onUserUpdate: user not null");
        }else{
            userIsNull = true;
            Log.d(TAG, "onUserUpdate: user null");
        }
        if(checkIfCalledByFile()){
            if (!userIsNull) {
                closeAfterImport = true;
                goToImport();
            }else{
                finish();
            }
        }
    }

    private void goToImport() {
        Intent newIntent = new Intent(getApplicationContext(),ImportActivity.class);
        newIntent.setData(getIntent().getData());
        fileImportResult.launch(newIntent);
    }

    private boolean checkIfCalledByFile() {
        return getIntent().getData() != null;
    }

    private void importResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if(result.getData() != null){
                Log.d(TAG, "importResult: ResultCode == RESULT_OK && Data != null");
            }else{
                Log.d(TAG, "importResult: ResultCode == RESULT_OK && Data == null");
            }
        }else{
            Log.d(TAG, "importResult: ResultCode == RESULT_CANCELED");
        }
        if(closeAfterImport)
            finish();
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
                showSnackBar(getResources().getText(R.string.double_tap_exit));
                doubleBackToExitPressedOnce = true;
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, doubleTapBack);
            }else{
                super.onBackPressed();
            }
        }
    }
}