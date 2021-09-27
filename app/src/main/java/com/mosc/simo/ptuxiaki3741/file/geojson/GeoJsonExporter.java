package com.mosc.simo.ptuxiaki3741.file.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GeoJsonExporter {
    public static JSONObject geoJsonExport(List<Land> lands){
        JSONObject
                featureJson,
                geometryJson,
                propertiesJson,
                mainJson = null;
        JSONArray featuresArray,
                coordinatesOuterBounds,
                coordinatesInnerBounds,
                coordinatesBounds;
        try{
            mainJson = new JSONObject();
            mainJson.put("type", "FeatureCollection");
            mainJson.put("totalFeatures", lands.size());
            int number = 1;

            featuresArray = new JSONArray();
            for (Land land : lands) {
                featureJson = new JSONObject();
                featureJson.put("type", "Feature");
                featureJson.put("id", number+"-"+land.getData().getTitle());

                geometryJson = new JSONObject();

                geometryJson.put("type", "Polygon");
                coordinatesOuterBounds = new JSONArray();
                coordinatesInnerBounds = new JSONArray();
                for(LatLng point : land.getData().getBorder()){
                    coordinatesBounds = new JSONArray();
                    coordinatesBounds.put(point.longitude);
                    coordinatesBounds.put(point.latitude);
                    coordinatesInnerBounds.put(coordinatesBounds);
                }
                coordinatesOuterBounds.put(coordinatesInnerBounds);

                geometryJson.put("coordinates", coordinatesOuterBounds);

                featureJson.put("geometry",geometryJson);

                featureJson.put("geometry_name","the_geom");

                propertiesJson = new JSONObject();
                propertiesJson.put("PER",land.getData().getTitle());
                featureJson.put("properties",propertiesJson);

                featuresArray.put(featureJson);
            }
            mainJson.put("features",featuresArray);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return mainJson;
    }
}
