package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GeoJsonExporter {
    public static JSONObject geoJsonExport(List<ExportGeoJsonFieldModel> fieldList){
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
            mainJson.put("totalFeatures", fieldList.size());

            featuresArray = new JSONArray();
            for (ExportGeoJsonFieldModel field : fieldList) {
                featureJson = new JSONObject();
                featureJson.put("type", "Feature");
                featureJson.put("id", field.getKey());

                geometryJson = new JSONObject();
                if(field.getPoints().size() > 0){
                    geometryJson.put("type", "Polygon");

                    coordinatesOuterBounds = new JSONArray();
                    coordinatesInnerBounds = new JSONArray();
                    for(List<Double> point : field.getPoints()){
                        coordinatesBounds = new JSONArray();
                        coordinatesBounds.put(point.get(0));
                        coordinatesBounds.put(point.get(1));
                        coordinatesInnerBounds.put(coordinatesBounds);
                    }
                    coordinatesOuterBounds.put(coordinatesInnerBounds);

                    geometryJson.put("coordinates", coordinatesOuterBounds);
                }
                featureJson.put("geometry",geometryJson);

                featureJson.put("geometry_name","the_geom");

                propertiesJson = new JSONObject();
                propertiesJson.put("PER",field.getTitle());
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
