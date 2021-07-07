package com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson;

import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GeoJsonExporter {
    public static JSONObject geoJsonExport(List<ExportFieldModel> fieldList){
        JSONObject
                featureJson,
                geometryJson,
                propertiesJson,
                mainJson = null;
        JSONArray featuresArray,
                coordinatesOuterBounds,
                coordinatesInnerBounds,
                coordinatesListBounds,
                coordinatesBounds;
        try{
            mainJson = new JSONObject();
            mainJson.put("type", "FeatureCollection");
            mainJson.put("totalFeatures", fieldList.size());

            featuresArray = new JSONArray();
            for (ExportFieldModel field : fieldList) {
                featureJson = new JSONObject();
                featureJson.put("type", "Feature");
                featureJson.put("id", field.getKey());

                geometryJson = new JSONObject();
                if(field.getPointsList().size() == 1){
                    geometryJson.put("type", "Polygon");
                    coordinatesOuterBounds = new JSONArray();
                    coordinatesInnerBounds = new JSONArray();
                    for(List<Double> point : field.getPointsList().get(0)){
                        coordinatesBounds = new JSONArray();
                        coordinatesBounds.put(point.get(0));
                        coordinatesBounds.put(point.get(1));
                        coordinatesInnerBounds.put(coordinatesBounds);
                    }
                    coordinatesOuterBounds.put(coordinatesInnerBounds);

                    geometryJson.put("coordinates", coordinatesOuterBounds);
                }else if(field.getPointsList().size() > 1){
                    geometryJson.put("type", "MultiPolygon");
                    coordinatesListBounds = new JSONArray();
                    for(List<List<Double>> points : field.getPointsList()){
                        coordinatesOuterBounds = new JSONArray();
                        coordinatesInnerBounds = new JSONArray();
                        for(List<Double> point : points){
                            coordinatesBounds = new JSONArray();
                            coordinatesBounds.put(point.get(0));
                            coordinatesBounds.put(point.get(1));
                            coordinatesInnerBounds.put(coordinatesBounds);
                        }
                        coordinatesOuterBounds.put(coordinatesInnerBounds);
                        coordinatesListBounds.put(coordinatesOuterBounds);
                    }
                    geometryJson.put("coordinates", coordinatesListBounds);
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
