package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityImportBinding;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {
    public static final String TAG = "ImportActivity";

    private List<Land> landList;
    private User curUser;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityImportBinding binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        initView();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        initMap();
    }
    @Override
    public void onPolygonClick(@NonNull Polygon polygon){
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

    private void init(){
        curUser = null;
        landList = new ArrayList<>();
        Intent intent = getIntent();
        if(intent != null){
            Log.d(TAG, "init: intent not null");
            if(intent.getExtras() != null){
                Log.d(TAG, "init: intent extra not null");
                long id = intent.getExtras().getLong(AppValues.userNameImportActivity,-1);
                AsyncTask.execute(()->
                        onUserUpdate(MainActivity.getRoomDb(this).userDao().getUserById(id))
                );
            }
            List<Land> fileLands = FileUtil.handleFile(getApplicationContext(),intent);
            landList.addAll(fileLands);
        }else{
            Log.d(TAG, "init: intent null");
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    private void initView(){
        if(UIUtil.isGooglePlayServicesAvailable(this)){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.ImportActivityRoot);
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