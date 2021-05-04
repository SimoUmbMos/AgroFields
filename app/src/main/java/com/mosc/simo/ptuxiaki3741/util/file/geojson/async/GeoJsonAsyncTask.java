package com.mosc.simo.ptuxiaki3741.util.file.geojson.async;

import android.util.Log;

import com.google.gson.Gson;
import com.mosc.simo.ptuxiaki3741.util.file.geojson.model.FeatureCollection;

import java.util.concurrent.Callable;


public class GeoJsonAsyncTask implements Callable<FeatureCollection> {
    public static final String TAG = "GeoJsonAsyncTask";
    private final String geojson;

    public GeoJsonAsyncTask(String geojson){
        this.geojson = geojson;
    }
    @Override
    public FeatureCollection call() {
        FeatureCollection featureCollection = null;
        try {
            featureCollection = new Gson().fromJson(geojson, FeatureCollection.class);
        }catch (Exception e){
            Log.e(TAG, "call: ", e);
        }
        return featureCollection;
    }
}
