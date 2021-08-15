package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
        }else{
            Toast.makeText(this, R.string.not_login_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void init(){
        curUser = null;
        Intent intent = getIntent();
        if(intent != null){
            long id = intent.getExtras().getLong(userName,-1);
            AsyncTask.execute(()->
                    onUserUpdate(MainActivity.getRoomDb(this).userDao().getUserById(id))
            );
            polyList = new ArrayList<>();
            polyList.addAll(FileUtil.handleFile(getApplicationContext(),intent));
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    private void initView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null){
            mapFragment.getMapAsync(this);
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
            Log.d(TAG, "onUserUpdate: user not null");
            if(polyList.size() == 1){
                Intent returnIntent;
                returnIntent = new Intent();
                returnIntent.putExtra(resName,new ParcelablePolygon(polyList.get(0)));
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        }else{
            Log.d(TAG, "onUserUpdate: user null");
        }
    }

    private void onMapFullLoaded(){
        showPointsOnMap();
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
        }
    }
}