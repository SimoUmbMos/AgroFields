package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityImportBinding;
import com.mosc.simo.ptuxiaki3741.enums.ImportAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {
    public static final String TAG = "ImportActivity";
    private List<Land> landList;
    private User curUser;
    private GoogleMap mMap;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityImportBinding binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init(binding);
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override public void onMapReady(@NonNull GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        initMap();
    }
    @Override public void onPolygonClick(@NonNull Polygon polygon){
        if(curUser != null){
            Intent returnIntent;
            returnIntent = new Intent();
            returnIntent.putExtra(
                    AppValues.resNameImportActivity,
                    new Land(new LandData(polygon.getPoints(),polygon.getHoles()))
            );
            setResult(RESULT_OK,returnIntent);
            finish();
        }
    }

    private void init(ActivityImportBinding binding){
        setSupportActionBar(binding.tbImportActivity);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        initThemeSettings();
        initData();
        initActivity();
    }
    public void initThemeSettings() {
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
    private void initData(){
        ImportAction action = ImportAction.VIEW;
        Intent intent = getIntent();
        if(intent != null){
            List<Land> fileLands = new ArrayList<>(
                    FileUtil.handleFile(getApplicationContext(),intent)
            );
            if(intent.getExtras() != null){
                if(intent.getExtras().containsKey(AppValues.userNameImportActivity)){
                    action = ImportAction.IMPORT;
                    long id = intent.getExtras().getLong(AppValues.userNameImportActivity,-1);
                    AsyncTask.execute(()->{
                        UserViewModel userViewModel =
                                new ViewModelProvider(this).get(UserViewModel.class);
                        onUserUpdate(userViewModel.getUserByID(id));
                    });
                }
                if(intent.getExtras().containsKey(AppValues.actionImportActivity)){
                    if(intent.getExtras().getBoolean(
                            AppValues.actionImportActivity,
                            false
                    )){
                        action = ImportAction.ADD;
                    }else{
                        action = ImportAction.SUBTRACT;
                    }
                }
            }
            landList = new ArrayList<>(fileLands);
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
        initActionBar(action);
    }
    private void initActivity(){
        if(UIUtil.isGooglePlayServicesAvailable(this)){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.ImportActivityMap);
            if(mapFragment != null){
                mapFragment.getMapAsync(this);
            }else{
                setResult(RESULT_CANCELED);
                finish();
            }
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    private void initActionBar(ImportAction action) {
        if(getSupportActionBar() != null){
            switch (action){
                case IMPORT:
                    getSupportActionBar().show();
                    getSupportActionBar().setTitle(
                            getString(R.string.import_activity_import_action)
                    );
                    break;
                case ADD:
                    getSupportActionBar().show();
                    getSupportActionBar().setTitle(
                            getString(R.string.import_activity_add_action)
                    );
                    break;
                case SUBTRACT:
                    getSupportActionBar().show();
                    getSupportActionBar().setTitle(
                            getString(R.string.import_activity_subtract_action)
                    );
                    break;
                case VIEW:
                default:
                    getSupportActionBar().hide();
                    break;
            }
        }
    }
    private void initMap(){
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMapLoadedCallback(this::onMapFullLoaded);
    }

    private void onUserUpdate(User user) {
        curUser = user;
        if(user != null){
            if(landList.size() == 1){
                Intent returnIntent;
                returnIntent = new Intent();
                returnIntent.putExtra(AppValues.resNameImportActivity,landList.get(0));
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        }
        if(mMap != null){
            toggleMapLock(user == null);
        }
    }

    private void toggleMapLock(boolean b){
        if(mMap != null){
            if(b){
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
            }else{
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
            }
        }
    }
    private void onMapFullLoaded(){
        showPointsOnMap();
        onUserUpdate(curUser);
    }
    private void showPointsOnMap(){
        int strokeColor = ContextCompat.getColor(this, R.color.polygonStroke);
        int fillColor = ContextCompat.getColor(this, R.color.polygonFill);
        int num = 0;

        if(landList.size() > 0){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(Land land : landList){
                PolygonOptions options = MapUtil.getPolygonOptions(
                        land,
                        strokeColor,
                        fillColor,
                        true
                );
                if(options != null){
                    Polygon polygon = mMap.addPolygon(options);
                    for(LatLng point : polygon.getPoints()){
                        builder.include(point);
                        num++;
                    }
                }
            }
            if(num > 0){
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), AppValues.defaultPadding));
            }
        }
    }
}