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
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final ActionResult<Float> onBearingUpdate;

    private boolean isRunning, autoGesture, isAccelInit, isMagnetInit;
    private float lastBearing;

    public SensorOrientationHelper(Context base, ActionResult<Float> onBearingUpdate) {
        super(base);
        this.onBearingUpdate = onBearingUpdate;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        isRunning = false;
        autoGesture = false;
        isAccelInit = false;
        isMagnetInit = false;
        lastBearing = -1;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
            isAccelInit = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
            isMagnetInit = true;
        }

        if(autoGesture){
            updateOrientationAngles();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onResume(){
        if(!isRunning){
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            }
            Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (magneticField != null) {
                sensorManager.registerListener(this, magneticField,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            }
            isRunning = true;
        }
    }
    public void onPause(){
        if(isRunning){
            sensorManager.unregisterListener(this);
            isAccelInit = false;
            isMagnetInit = false;
            lastBearing = -1;
            isRunning = false;
        }
    }

    public void setAutoGesture(boolean autoGesture) {
        this.autoGesture = autoGesture;
    }
    public boolean isAutoGesture() {
        return autoGesture;
    }

    private void updateOrientationAngles() {
        if(isAccelInit && isMagnetInit){
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null,
                    accelerometerReading, magnetometerReading);
            if(success){
                SensorManager.getOrientation(rotationMatrix, orientationAngles);
                float bearing = orientationAngles[0];
                bearing = (float) Math.floor( Math.toDegrees( bearing ) );
                if(lastBearing == -1 || Math.abs(lastBearing - bearing) >= AppValues.bearingSensitivity){
                    lastBearing = bearing;
                    onBearingUpdate.onActionResult(bearing);
                }
            }
        }
    }
}
