package com.mosc.simo.ptuxiaki3741.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class SensorOrientationHelper extends ContextWrapper implements SensorEventListener{
    private final SensorManager sensorManager;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private final ActionResult<Float> onBearingUpdate;
    private float Alpha;
    private int accelAccuracy, magAccuracy;

    private boolean isRunning, autoGesture;

    public SensorOrientationHelper(Context base, ActionResult<Float> onBearingUpdate) {
        super(base);
        this.onBearingUpdate = onBearingUpdate;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        isRunning = false;
        autoGesture = false;
        mGravity = null;
        mGeomagnetic = null;
        accelAccuracy = 0;
        magAccuracy = 0;
        Alpha = AppValues.bearingAlphaLow;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = applyLowPassFilter(event.values.clone(), mGravity);
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = applyLowPassFilter(event.values.clone(), mGeomagnetic);
        if(mGravity != null && mGeomagnetic != null && autoGesture){
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success){
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                double azimuth = Math.toDegrees(orientation[0]);
                azimuth = Math.ceil(azimuth);
                if(azimuth > 360) azimuth = azimuth - 360;
                if(azimuth < 0) azimuth = azimuth + 360;
                onBearingUpdate.onActionResult((float)azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelAccuracy = accuracy;
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magAccuracy = accuracy;

        switch (Math.min(accelAccuracy, magAccuracy)){
            case (SensorManager.SENSOR_STATUS_NO_CONTACT):
            case (SensorManager.SENSOR_STATUS_UNRELIABLE):
                Alpha = AppValues.bearingAlphaNone;
                break;
            case (SensorManager.SENSOR_STATUS_ACCURACY_LOW):
                Alpha = AppValues.bearingAlphaLow;
                break;
            case (SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM):
                Alpha = AppValues.bearingAlphaMedium;
                break;
            case (SensorManager.SENSOR_STATUS_ACCURACY_HIGH):
                Alpha = AppValues.bearingAlphaHigh;
                break;
        }
    }

    public void onResume(){
        if(!isRunning){
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_GAME
                );
            }
            Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (magneticField != null) {
                sensorManager.registerListener(
                        this,
                        magneticField,
                        SensorManager.SENSOR_DELAY_GAME
                );
            }
            isRunning = true;
        }
    }
    public void onPause(){
        if(isRunning){
            sensorManager.unregisterListener(this);
            isRunning = false;
            mGravity = null;
            mGeomagnetic = null;
        }
    }

    public void setAutoGesture(boolean autoGesture) {
        this.autoGesture = autoGesture;
    }
    public boolean isAutoGesture() {
        return autoGesture;
    }

    private float[] applyLowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + Alpha * (input[i] - output[i]);
        }
        return output;
    }
}
