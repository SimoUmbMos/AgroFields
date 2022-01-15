package com.mosc.simo.ptuxiaki3741.helpers;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

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
    private static final String TAG = "LocationHelper";
    private final FusedLocationProviderClient fusedLocationClient;
    private HandlerThread mLocationThread;
    private final LocationCallback locationCallback;

    private ActionResult<Location> onLocationUpdate;
    private LocationStates locationPermission;
    private boolean locationUpdateRunning;
    private Location lastLocation;
    private int interval, fastInterval;

    public LocationHelper(Context base, ActionResult<Location> l) {
        super(base);
        this.onLocationUpdate = l;
        interval = 10000;
        fastInterval = 1000;
        lastLocation = null;
        locationPermission = LocationStates.DISABLE;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                boolean doUpdate = false;
                if (lastLocation != null) {
                    if (lastLocation.distanceTo(location) > AppValues.locationSensitivity) {
                        doUpdate = true;
                    }
                } else {
                    doUpdate = true;
                }
                if (doUpdate) {
                    Log.d(TAG, "onLocationResult: update");
                    lastLocation = location;
                    onLocationUpdate.onActionResult(location);
                }
            }
        };
    }

    public void setOnLocationUpdate(ActionResult<Location> l) {
        this.onLocationUpdate = l;
    }

    public void setLocationPermission(LocationStates locationPermission) {
        boolean wasRunning = isRunning();
        if (wasRunning) {
            stop();
        }
        this.locationPermission = locationPermission;
        if (wasRunning) {
            start();
        }
    }
    public LocationStates getLocationPermission() {
        return locationPermission;
    }
    public void setInterval(int interval) {
        boolean wasRunning = isRunning();
        if (wasRunning) {
            stop();
        }
        this.interval = interval;
        if (wasRunning) {
            start();
        }
    }
    public void setFastInterval(int fastInterval) {
        boolean wasRunning = isRunning();
        if (wasRunning) {
            stop();
        }
        this.fastInterval = fastInterval;
        if (wasRunning) {
            start();
        }
    }


    public boolean isRunning() {
        return locationUpdateRunning;
    }

    public void start() {
        if (isRunning()) {
            stop();
        }
        if(fusedLocationClient != null){
            if(locationPermission == LocationStates.FINE_LOCATION){
                startFineLocationUpdates();
            }else if(locationPermission == LocationStates.COARSE_LOCATION){
                startCoarseLocationUpdates();
            }
        }
    }
    public void stop() {
        if (fusedLocationClient == null) return;

        if (locationUpdateRunning) {
            fusedLocationClient.removeLocationUpdates(locationCallback);

            if (mLocationThread != null) {
                if (mLocationThread.isAlive()) {
                    mLocationThread.quitSafely();
                }
            }

            locationUpdateRunning = false;

            Log.d(TAG, "stopLocationUpdates: ended");
        }
    }

    public void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(
                    location -> onLocationUpdate.onActionResult(location)
            );
        }else{
            onLocationUpdate.onActionResult(null);
        }
    }
    private void startFineLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(interval)
                    .setFastestInterval(fastInterval)
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
            Log.d(TAG, "Fine Location Updates: started");
        }
    }
    private void startCoarseLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(interval)
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
            Log.d(TAG, "Coarse Location Updates: started");
        }
    }
}
