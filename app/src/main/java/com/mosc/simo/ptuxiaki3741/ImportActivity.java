package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.UserViewModel;
import com.mosc.simo.ptuxiaki3741.databinding.ActivityImportBinding;
import com.mosc.simo.ptuxiaki3741.fragmentrelated.helper.FileHelper;
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {
    public static final String TAG = "ImportActivity";
    public static final String resName = "polygon";

    private List<List<LatLng>> polyList;
    private User curUser;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityImportBinding binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViewModel();
        initView();
        init();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap){
        mMap = googleMap;
        initMap();
        mMap.setOnMapLoadedCallback(this::onMapFullLoaded);
    }

    private void initViewModel(){
        curUser = null;
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getCurrUser().observe(this,newUser -> curUser = newUser);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userViewModel.init(sharedPref);
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

    private void init(){
        Intent intent = getIntent();
        if(intent != null){
            polyList = new ArrayList<>();
            FileHelper fileHelper = new FileHelper(this);
            polyList.addAll(fileHelper.handleFile(intent));
            Log.d(TAG, "onCreate: "+polyList.size());
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void initMap(){
        mMap.setOnPolygonClickListener(this);
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
}