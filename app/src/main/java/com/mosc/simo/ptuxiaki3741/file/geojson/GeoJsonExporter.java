package com.mosc.simo.ptuxiaki3741.file.geojson;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class GeoJsonExporter {
    private static final String TAG = "GeoJsonHelper";
    public static JSONObject geoJsonExport(List<Land> lands){
        List<List<List<LatLng>>> exportData = new ArrayList<>();

        for(Land land : lands){
            if(land.getData() == null) continue;

            List<List<LatLng>> pointsList = new ArrayList<>();
            pointsList.add(land.getData().getBorder());
            pointsList.addAll(land.getData().getHoles());

            exportData.add(pointsList);
        }

        return toGeoJson(exportData);
    }
    public static JSONObject toGeoJson(List<List<List<LatLng>>> exportData){
        JSONObject main, featureJson, geometryJson;
        JSONArray outerBounds, innerBounds, coordinatesBounds;
        try{
            JSONArray featuresArray = new JSONArray();
            for(List<List<LatLng>> pointsList : exportData){
                outerBounds = new JSONArray();
                for(List<LatLng> points : pointsList){
                    innerBounds = new JSONArray();

                    for(LatLng point : points){
                        coordinatesBounds = new JSONArray();

                        coordinatesBounds.put(point.longitude);
                        coordinatesBounds.put(point.latitude);

                        innerBounds.put(coordinatesBounds);
                    }
                    if(points.size() > 0){
                        coordinatesBounds = new JSONArray();

                        coordinatesBounds.put(points.get(0).longitude);
                        coordinatesBounds.put(points.get(0).latitude);

                        innerBounds.put(coordinatesBounds);
                    }

                    outerBounds.put(innerBounds);
                }

                geometryJson = new JSONObject();
                geometryJson.put("type", "Polygon");
                geometryJson.put("coordinates",outerBounds);

                featureJson = new JSONObject();
                featureJson.put("type", "Feature");
                featureJson.put("geometry",geometryJson);
                featureJson.put("properties",new JSONObject());

                featuresArray.put(featureJson);
            }

            main = new JSONObject();
            main.put("type", "FeatureCollection");
            main.put("features",featuresArray);
        }catch (Exception e){
            Log.e(TAG, "geoJsonExport: ", e);
            main = new JSONObject();
        }
        return main;
    }
}
