package com.mosc.simo.ptuxiaki3741;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
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
import com.mosc.simo.ptuxiaki3741.models.ParcelablePolygon;

import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {
    public static final String argName = "points";
    public static final String resName = "polygon";

    private List<List<LatLng>> polyList;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityImportBinding binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getParameters(getIntent().getExtras());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null){
            mapFragment.getMapAsync(this);
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
        mMap.setOnMapLoadedCallback(this::onMapFullLoaded);
    }

    private void initMap() {
        mMap.setOnPolygonClickListener(this);
    }

    private void onMapFullLoaded() {
        showPointsOnMap();
    }

    private void showPointsOnMap() {
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

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(resName,new ParcelablePolygon(polygon.getPoints()));
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    private void getParameters(Bundle extras) {
        ArrayList<ParcelablePolygon> tempList = extras.getParcelableArrayList(argName);
        polyList = new ArrayList<>();
        List<LatLng> tempPoints;
        for(ParcelablePolygon tempPoly : tempList){
            tempPoints = new ArrayList<>(tempPoly.getPoints());
            polyList.add(tempPoints);
        }
    }
}