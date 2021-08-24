package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
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
import com.mosc.simo.ptuxiaki3741.util.FileUtil;
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {
    public static final String TAG = "ImportActivity";
    public static final String resName = "polygon", userName = "userid";
    private List<List<LatLng>> polyList;
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        initMap();
    }
    @Override
    public void onPolygonClick(@NonNull Polygon polygon){
        if(curUser != null){
            Intent returnIntent;
            returnIntent = new Intent();
            returnIntent.putExtra(resName,new ParcelablePolygon(polygon.getPoints()));
            setResult(RESULT_OK,returnIntent);
            finish();
        }
    }

    private void init(){
        curUser = null;
        polyList = new ArrayList<>();
        Intent intent = getIntent();
        if(intent != null){
            Log.d(TAG, "init: intent not null");
            if(intent.getExtras() != null){
                Log.d(TAG, "init: intent extra not null");
                long id = intent.getExtras().getLong(userName,-1);
                AsyncTask.execute(()->
                        onUserUpdate(MainActivity.getRoomDb(this).userDao().getUserById(id))
                );
            }
            polyList.addAll(FileUtil.handleFile(getApplicationContext(),intent));
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
            if(polyList.size() == 1){
                Intent returnIntent;
                returnIntent = new Intent();
                returnIntent.putExtra(resName,new ParcelablePolygon(polyList.get(0)));
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
        if(polyList.size() > 0){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(List<LatLng> points : polyList){
                mMap.addPolygon(new PolygonOptions()
                        .clickable(true)
                        .addAll(points)
                );
                for(LatLng point : points){
                    builder.include(point);
                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 64));
        }
    }
}