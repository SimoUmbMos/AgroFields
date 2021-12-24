package com.mosc.simo.ptuxiaki3741.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

public class SensorOrientationHelper extends ContextWrapper {
    private final SensorEventListener sensorCallback;
    private HandlerThread mSensorThreadAccelerometer;
    private HandlerThread mSensorThreadMagneticField;
    private boolean autoGesture;
    private Float lastBearing;

    public SensorOrientationHelper(Context base, ActionResult<Float> onBearingUpdate) {
        super(base);
        autoGesture = false;
        lastBearing = null;

        sensorCallback = new SensorEventListener() {
            private float[] mGravity;
            private float[] mGeomagnetic;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (!autoGesture) return;

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mGravity = event.values;
                } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
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
                        bearing = (float) (Math.toDegrees(bearing) + 360) % 360;
                        bearing = (float) Math.floor(bearing);
                        boolean doUpdate = false;
                        if(lastBearing != null){
                            if(Math.abs(lastBearing - bearing) > AppValues.bearingSensitivity) {
                                doUpdate = true;
                            }
                        }else{
                            doUpdate = true;
                        }
                        if(doUpdate){
                            lastBearing = bearing;
                            onBearingUpdate.onActionResult(bearing);
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    public void onResume(){
        if(mSensorThreadAccelerometer != null) {
            if (mSensorThreadAccelerometer.isAlive()) {
                mSensorThreadAccelerometer.quitSafely();
            }
        }
        if(mSensorThreadMagneticField != null) {
            if (mSensorThreadMagneticField.isAlive()) {
                mSensorThreadMagneticField.quitSafely();
            }
        }

        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            mSensorThreadAccelerometer = new HandlerThread("Accelerometer Sensor Thread", Process.THREAD_PRIORITY_LESS_FAVORABLE);
            mSensorThreadAccelerometer.start();
            Handler mSensorHandlerAccelerometer = new Handler(mSensorThreadAccelerometer.getLooper());
            mSensorManager.registerListener(
                    sensorCallback,
                    mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mSensorHandlerAccelerometer
            );
        }

        Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mMagneticField != null) {
            mSensorThreadMagneticField = new HandlerThread("Magnetic Field Sensor Thread", Process.THREAD_PRIORITY_LESS_FAVORABLE);
            mSensorThreadMagneticField.start();
            Handler mSensorHandlerMagneticField = new Handler(mSensorThreadAccelerometer.getLooper());
            mSensorManager.registerListener(
                    sensorCallback,
                    mMagneticField,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mSensorHandlerMagneticField
            );
        }
    }
    public void onPause(){
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(sensorCallback);

        if(mSensorThreadAccelerometer != null) {
            if (mSensorThreadAccelerometer.isAlive()) {
                mSensorThreadAccelerometer.quitSafely();
            }
        }
        if(mSensorThreadMagneticField != null) {
            if (mSensorThreadMagneticField.isAlive()) {
                mSensorThreadMagneticField.quitSafely();
            }
        }
    }

    public void setAutoGesture(boolean autoGesture) {
        this.autoGesture = autoGesture;
    }
    public boolean isAutoGesture() {
        return autoGesture;
    }
}
