package com.mosc.simo.ptuxiaki3741.fragments.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.databinding.FragmentContactsContainerBinding;
import com.mosc.simo.ptuxiaki3741.enums.LocationStates;
import com.mosc.simo.ptuxiaki3741.interfaces.FragmentBackPress;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;
import com.mosc.simo.ptuxiaki3741.viewmodels.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsContainerFragment extends Fragment implements FragmentBackPress {
    private FragmentContactsContainerBinding binding;
    private ActionBar actionBar;
    private List<Land> lands;
    private Map<Long,List<LandZone>> zones;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationStates locationPermission;
    private boolean locationUpdateRunning;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private GoogleMap mMap;

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::onPermissionResult
    );

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactsContainerBinding.inflate(inflater,container,false);
        binding.mvLiveMap.onCreate(savedInstanceState);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActivity();
        initViewHolder();
        initFragment();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding.mvLiveMap.onDestroy();
        binding = null;
    }
    @Override public void onResume() {
        super.onResume();
        binding.mvLiveMap.onResume();
        startLocationUpdates();
    }
    @Override public void onPause() {
        super.onPause();
        binding.mvLiveMap.onPause();
        stopLocationUpdates();
    }
    @Override public void onLowMemory() {
        super.onLowMemory();
        binding.mvLiveMap.onLowMemory();
    }
    @Override public boolean onBackPressed() {
        return true;
    }

    private void initData() {
        locationPermission = LocationStates.DISABLE;
        locationUpdateRunning = false;
        fusedLocationClient = null;
        lastLocation = null;
        lands = new ArrayList<>();
        zones = new HashMap<>();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationUpdate(locationResult);
            }
        };
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                actionBar = activity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle("");
                    actionBar.show();
                }
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        }
    }
    private void initViewHolder(){
        if(getActivity() != null){
            AppViewModel appVM = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appVM.getLands().observe(getViewLifecycleOwner(),this::onLandsUpdate);
            appVM.getLandZones().observe(getViewLifecycleOwner(),this::onZonesUpdate);
        }
    }

    private void onLandsUpdate(List<Land> lands) {
        this.lands.clear();
        if(lands != null){
            this.lands.addAll(lands);
        }
        updateMap();
    }
    private void onZonesUpdate(Map<Long,List<LandZone>> zones) {
        this.zones.clear();
        if(zones != null)
            this.zones.putAll(zones);
        updateMap();
    }
    private void initFragment() {
        binding.mvLiveMap.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap){
        mMap = googleMap;

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        updateMap();
        requestPermissions();
    }

    private void updateMap(){
        if(mMap == null) return;

        mMap.clear();
        List<LandZone> temp;
        PolygonOptions options;
        int strokeColor;
        int fillColor;
        for(Land land : lands){
            strokeColor = Color.argb(
                AppValues.defaultStrokeAlpha,
                land.getData().getColor().getRed(),
                land.getData().getColor().getGreen(),
                land.getData().getColor().getBlue()
            );
            fillColor = Color.argb(
                AppValues.defaultFillAlpha,
                land.getData().getColor().getRed(),
                land.getData().getColor().getGreen(),
                land.getData().getColor().getBlue()
            );
            options = LandUtil.getPolygonOptions(
                land.getData(),
                strokeColor,
                fillColor,
                false
            );
            if(options != null){
                mMap.addPolygon(options.zIndex(1));
            }
            temp = zones.getOrDefault(land.getData().getId(), null);
            if(temp != null){
                for(LandZone zone:temp){
                    strokeColor = Color.argb(
                            AppValues.defaultStrokeAlpha,
                            zone.getData().getColor().getRed(),
                            zone.getData().getColor().getGreen(),
                            zone.getData().getColor().getBlue()
                    );
                    fillColor = Color.argb(
                            AppValues.defaultFillAlpha,
                            zone.getData().getColor().getRed(),
                            zone.getData().getColor().getGreen(),
                            zone.getData().getColor().getBlue()
                    );
                    options = LandUtil.getPolygonOptions(
                            zone.getData(),
                            strokeColor,
                            fillColor,
                            false
                    );
                    if(options != null){
                        mMap.addPolygon(options.zIndex(2));
                    }
                }
            }
        }
    }
    private void updateUi(){
        if(lastLocation == null)
            return;

        Land currentLand = null;
        LandZone currentZone = null;
        LatLng lastPoint = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        for(Land land:lands){
            if(MapUtil.contains(lastPoint,land.getData().getBorder())){
                currentLand = land;
                break;
            }
        }
        if(currentLand != null){
            List<LandZone> temp = zones.getOrDefault(currentLand.getData().getId(),null);
            if(temp != null){
                for(LandZone zone:temp){
                    if(MapUtil.contains(lastPoint,zone.getData().getBorder())){
                        currentZone = zone;
                        break;
                    }
                }
            }
        }

        displayNotification(currentLand,currentZone);
    }

    private void displayNotification(Land currentLand, LandZone currentZone) {
        String title = "";
        String display = "";
        boolean isNew = false;
        if(currentLand != null){
            if(currentZone != null){
                title = currentZone.toString();
                display = currentZone.getData().getNote();
            }else{
                title = currentLand.toString();
            }
        }
        if(actionBar != null){
            String prevTitle = "";
            if(actionBar.getTitle() != null){
                prevTitle = actionBar.getTitle().toString();
            }
            if(!prevTitle.equals(title)){
                actionBar.setTitle(title);
                isNew = true;
            }
            //fixme: show display
        }

        if(isNew){
            vibratePhone();
        }
    }

    private void vibratePhone() {
        if(getContext() == null) return;
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private void requestPermissions() {
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        Boolean fineLocationGranted = result.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION,false);

        if (fineLocationGranted != null && fineLocationGranted){
            locationPermission = LocationStates.FINE_LOCATION;
        }else if(coarseLocationGranted != null && coarseLocationGranted){
            locationPermission = LocationStates.COARSE_LOCATION;
        }else{
            locationPermission = LocationStates.DISABLE;
        }

        startLocationUpdates();
    }
    private void onLocationUpdate(LocationResult locationResult){
        Location location = locationResult.getLastLocation();
        if(lastLocation == null){
            updateLocation(location);
        }else if(
                lastLocation.getLongitude() != location.getLongitude() &&
                lastLocation.getLatitude() != location.getLatitude()
        ){
            updateLocation(location);
        }
    }

    private void updateLocation(Location location) {
        if(mMap != null){
            CameraPosition.Builder position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                    .bearing(location.getBearing());
            if(lastLocation != null){
                mMap.stopAnimation();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position
                        .tilt(mMap.getCameraPosition().tilt)
                        .zoom(mMap.getCameraPosition().zoom)
                        .build()
                ));
            }else{
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position
                        .tilt(AppValues.personTilt)
                        .zoom(AppValues.personZoom)
                        .build()
                ));
            }
        }
        lastLocation = location;
        updateUi();
    }

    private void startLocationUpdates() {
        switch (locationPermission){
            case FINE_LOCATION:
                startFineLocationUpdates();
                break;
            case COARSE_LOCATION:
                startCoarseLocationUpdates();
                break;
            case DISABLE:
            default:
                stopLocationUpdates();
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void startCoarseLocationUpdates() {
        if(fusedLocationClient == null) return;
        if(locationPermission != LocationStates.COARSE_LOCATION) return;

        if(locationUpdateRunning){
            stopLocationUpdates();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        if(mMap != null) mMap.setMyLocationEnabled(true);

        locationUpdateRunning = true;
    }

    @SuppressLint("MissingPermission")
    private void startFineLocationUpdates() {
        if(fusedLocationClient == null) return;
        if(locationPermission != LocationStates.FINE_LOCATION) return;

        if(locationUpdateRunning){
            stopLocationUpdates();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        if(mMap != null) mMap.setMyLocationEnabled(true);

        locationUpdateRunning = true;
    }

    @SuppressLint("MissingPermission")
    private void stopLocationUpdates() {
        if(fusedLocationClient == null) return;

        if(mMap != null) mMap.setMyLocationEnabled(false);

        if(locationUpdateRunning){
            fusedLocationClient.removeLocationUpdates(locationCallback);

            locationUpdateRunning = false;
        }
    }
}