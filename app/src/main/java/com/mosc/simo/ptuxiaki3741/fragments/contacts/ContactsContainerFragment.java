package com.mosc.simo.ptuxiaki3741.fragments.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.R;
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

public class ContactsContainerFragment extends Fragment implements FragmentBackPress{
    public static final String TAG = "ContactsContainerFragment";
    private FragmentContactsContainerBinding binding;
    private ActionBar actionBar;
    private List<Land> lands;
    private Map<Long,List<LandZone>> zones;
    private GoogleMap mMap;
    private float lastBearing, currTilt, currZoom, currBearing;
    private Location lastLocation;
    private boolean autoGesture,hasUserMovedCamera;
    private Circle currPosition;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationStates locationPermission;
    private LocationCallback locationCallback;
    private SensorEventListener sensorCallback;
    private HandlerThread mSensorThreadAccelerometer, mSensorThreadMagneticField,mLocationThread;
    private boolean locationUpdateRunning;


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

        if(getActivity() != null){
            SensorManager mSensorManager =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


            if (mAccelerometer != null) {
                mSensorThreadAccelerometer = new HandlerThread("Sensor thread", android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
                mSensorThreadAccelerometer.start();
                Handler mSensorHandlerAccelerometer = new Handler(mSensorThreadAccelerometer.getLooper());
                mSensorManager.registerListener(
                        sensorCallback,
                        mAccelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL ,
                        mSensorHandlerAccelerometer
                );
            }
            if (mMagneticField != null) {
                mSensorThreadMagneticField = new HandlerThread("Sensor thread", Process.THREAD_PRIORITY_LESS_FAVORABLE);
                mSensorThreadMagneticField.start();
                Handler mSensorHandlerMagneticField = new Handler(mSensorThreadAccelerometer.getLooper());
                mSensorManager.registerListener(
                        sensorCallback,
                        mMagneticField,
                        SensorManager.SENSOR_DELAY_NORMAL ,
                        mSensorHandlerMagneticField
                );
            }
        }

        startLocationUpdates();
    }
    @Override public void onPause() {
        super.onPause();

        stopLocationUpdates();

        if(getActivity() != null){
            SensorManager mSensorManager =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            mSensorManager.unregisterListener(
                    sensorCallback
            );
            if(mSensorThreadAccelerometer != null){
                mSensorThreadAccelerometer.quitSafely();
            }
            if(mSensorThreadMagneticField != null){
                mSensorThreadMagneticField.quitSafely();
            }
        }

        binding.mvLiveMap.onPause();
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
        hasUserMovedCamera = false;
        fusedLocationClient = null;
        lastLocation = null;
        lastBearing = 0;
        lands = new ArrayList<>();
        zones = new HashMap<>();
        sensorCallback = new SensorEventListener(){
            private float[] mGravity;
            private float[] mGeomagnetic;
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(!autoGesture) return;

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    mGravity = event.values;
                }else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                    mGeomagnetic = event.values;
                }

                if (mGravity != null && mGeomagnetic != null) {
                    float[] R = new float[9];
                    float[] I = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                            mGeomagnetic);
                    if (success) {
                        float[] orientation = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        float bearing = orientation[0];
                        bearing = (float) (Math.toDegrees(bearing)+360) % 360;
                        bearing = (float)  Math.floor(bearing);
                        if(Math.abs(bearing - lastBearing) >= AppValues.bearingSensitivity){
                            onUpdateBearing(bearing);
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationUpdate(locationResult);
            }
        };
        startAutoGesture();
    }
    private void initActivity() {
        if(getActivity() != null){
            if(getActivity().getClass() == MainActivity.class){
                MainActivity activity = (MainActivity) getActivity();
                activity.setOnBackPressed(this);
                actionBar = activity.getSupportActionBar();
                if(actionBar != null){
                    actionBar.setTitle(getString(R.string.default_live_map_title));
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
    private void initFragment() {
        binding.mvLiveMap.getMapAsync(this::initMap);
    }
    private void initMap(GoogleMap googleMap){
        binding.mvLiveMap.setVisibility(View.INVISIBLE);

        mMap = googleMap;

        mMap.setMinZoomPreference(AppValues.cityZoom);
        mMap.setMaxZoomPreference(AppValues.personZoom);

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        currBearing = mMap.getCameraPosition().bearing;
        currTilt = AppValues.defaultTilt;
        currZoom = AppValues.streetZoom;
        mMap.setOnCameraMoveListener(()->{
            currBearing = mMap.getCameraPosition().bearing;
            currTilt = mMap.getCameraPosition().tilt;
            currZoom = mMap.getCameraPosition().zoom;

            if(lastLocation != null){
                if(currPosition != null){
                    double radius = AppValues.currPositionSize * Math.pow(AppValues.currPowSize, AppValues.personZoom - currZoom);
                    radius = Math.floor(radius);
                    double radiusStroke = AppValues.currPositionSizeStroke * Math.pow(AppValues.currPowSizeStroke, AppValues.personZoom - currZoom);
                    radiusStroke = Math.floor(radiusStroke);
                    currPosition.setRadius(radius);
                    currPosition.setStrokeWidth((float)radiusStroke);
                }
            }
        });
        mMap.setOnCameraMoveStartedListener(id->{
            if(id == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
                hasUserMovedCamera = true;
        });

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
        String title = getString(R.string.default_live_map_title);
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

        mLocationThread = new HandlerThread("MyHandlerThread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mLocationThread.start();
        Looper looper = mLocationThread.getLooper();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                looper
        );

        locationUpdateRunning = true;
    }
    @SuppressLint("MissingPermission")
    private void startFineLocationUpdates() {
        if(fusedLocationClient == null) return;
        if(locationPermission != LocationStates.FINE_LOCATION) return;

        if(locationUpdateRunning){
            stopLocationUpdates();
        }

        mLocationThread = new HandlerThread("MyHandlerThread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mLocationThread.start();
        Looper looper = mLocationThread.getLooper();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                looper
        );

        locationUpdateRunning = true;
    }
    @SuppressLint("MissingPermission")
    private void stopLocationUpdates() {
        if(fusedLocationClient == null) return;

        if(locationUpdateRunning){
            fusedLocationClient.removeLocationUpdates(locationCallback);

            if(mLocationThread != null) mLocationThread.quitSafely();

            locationUpdateRunning = false;
        }
    }

    private void startAutoGesture(){
        autoGesture = true;
        //fixme: init ui
    }
    private void stopAutoGesture(){
        autoGesture = false;
        //fixme: init ui
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

        if(locationPermission != LocationStates.DISABLE){
            startLocationUpdates();
        }else{
            goBack();
        }
    }
    private void onLocationUpdate(LocationResult locationResult){
        Location location = locationResult.getLastLocation();
        LatLng locationLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        if(lastLocation == null){
            onUpdateLocation(location);
        }else{
            LatLng lastLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            if(MapUtil.distanceBetween(lastLatLng,locationLatLng) > AppValues.locationSensitivity){
                onUpdateLocation(location);
            }
        }
    }
    private void onUpdateLocation(Location location) {
        if(location == null) return;
        if(mMap == null) return;

        CameraPosition position;
        boolean isInit;
        if(lastLocation == null){
            position= new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                    .bearing(location.getBearing())
                    .tilt(AppValues.defaultTilt)
                    .zoom(AppValues.streetZoom)
                    .build();
            isInit = true;
        }else{
            position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                    .bearing(currBearing)
                    .tilt(currTilt)
                    .zoom(currZoom)
                    .build();
            isInit = false;
        }
        lastLocation = location;
        if(getActivity() != null){
            getActivity().runOnUiThread(()->{
                if(isInit){
                    if(currPosition != null)
                        currPosition.remove();
                    currPosition = mMap.addCircle(new CircleOptions()
                            .center(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()))
                            .fillColor(AppValues.defaultPersonColorFill)
                            .strokeColor(AppValues.defaultPersonColorStroke)
                            .zIndex(3)
                    );
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    binding.mvLiveMap.setVisibility(View.VISIBLE);
                }else{
                    if(currPosition != null){
                        currPosition.setCenter(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()));
                    }
                    mMap.stopAnimation();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                }
                updateUi();
            });
        }
    }
    private void onUpdateBearing(float bearing) {
        if(lastLocation == null) return;

        if (checkIfUserChangedBearing()) {
            stopAutoGesture();
            return;
        }
        lastBearing = bearing;
        if(mMap != null && getActivity() != null){
            getActivity().runOnUiThread(()->{
                mMap.stopAnimation();
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()))
                        .bearing(lastBearing)
                        .tilt(currTilt)
                        .zoom(currZoom)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            });
        }
    }

    private boolean checkIfUserChangedBearing() {
        if(hasUserMovedCamera){
            hasUserMovedCamera = false;
            if(Math.abs(lastBearing - currBearing) >= AppValues.bearingSensitivity) {
                return Math.abs(lastLocation.getBearing() - currBearing) >= AppValues.bearingSensitivity;
            }
        }
        return false;
    }

    private void goBack(){
        Toast.makeText(
                getContext(),
                getString(R.string.live_map_permission_error),
                Toast.LENGTH_SHORT
        ).show();
        if(getActivity() != null) getActivity().onBackPressed();
    }
}