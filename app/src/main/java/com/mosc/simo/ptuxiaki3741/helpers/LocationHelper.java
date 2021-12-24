package com.mosc.simo.ptuxiaki3741.helpers;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mosc.simo.ptuxiaki3741.enums.LocationStates;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class LocationHelper extends ContextWrapper {
    private final FusedLocationProviderClient fusedLocationClient;
    private HandlerThread mLocationThread;
    private final LocationCallback locationCallback;

    private LocationStates locationPermission;
    private boolean locationUpdateRunning;
    private Location lastLocation;

    public LocationHelper(Context base, ActionResult<Location> onLocationUpdate) {
        super(base);
        lastLocation = null;
        locationPermission = LocationStates.DISABLE;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                boolean doUpdate = false;
                if(lastLocation != null){
                    if(lastLocation.distanceTo(location) > AppValues.locationSensitivity){
                        doUpdate = true;
                    }
                }else{
                    doUpdate = true;
                }
                if(doUpdate){
                    lastLocation = location;
                    onLocationUpdate.onActionResult(location);
                }
            }
        };
    }

    public void onResume(){
        startLocationUpdates();
    }
    public void onPause(){
        stopLocationUpdates();
    }

    public void setLocationPermission(LocationStates locationPermission) {
        onPause();
        this.locationPermission = locationPermission;
        onResume();
    }

    private void startLocationUpdates() {
        if(locationPermission == null) return;
        switch (locationPermission) {
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
    private void startCoarseLocationUpdates() {
        if (fusedLocationClient == null) return;
        if (locationPermission != LocationStates.COARSE_LOCATION) return;
        if (locationUpdateRunning) stopLocationUpdates();

        if (mLocationThread != null) {
            if (mLocationThread.isAlive()) {
                mLocationThread.quitSafely();
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mLocationThread = new HandlerThread("Location Thread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
            mLocationThread.start();
            Looper looper = mLocationThread.getLooper();
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    looper
            );
            locationUpdateRunning = true;
        }
    }
    private void startFineLocationUpdates() {
        if (fusedLocationClient == null) return;
        if (locationPermission != LocationStates.FINE_LOCATION) return;
        if (locationUpdateRunning) stopLocationUpdates();

        if (mLocationThread != null) {
            if (mLocationThread.isAlive()) {
                mLocationThread.quitSafely();
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setFastestInterval(1000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mLocationThread = new HandlerThread("Location Thread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
            mLocationThread.start();
            Looper looper = mLocationThread.getLooper();
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    looper
            );
            locationUpdateRunning = true;
        }
    }
    private void stopLocationUpdates() {
        if(fusedLocationClient == null) return;

        if(locationUpdateRunning){
            fusedLocationClient.removeLocationUpdates(locationCallback);

            if (mLocationThread != null) {
                if (mLocationThread.isAlive()) {
                    mLocationThread.quitSafely();
                }
            }

            locationUpdateRunning = false;
        }
    }
}
