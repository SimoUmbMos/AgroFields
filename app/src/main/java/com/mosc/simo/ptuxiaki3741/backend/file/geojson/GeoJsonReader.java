package com.mosc.simo.ptuxiaki3741.backend.file.geojson;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.helpers.CoordinatesHelper;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonReader {
    public static ArrayList<LandData> exec(Context context, InputStream input) throws JSONException{
        ArrayList<LandData> result = new ArrayList<>();
        if(input == null) return result;
        JSONObject jsonObject = new JSONObject(DataUtil.inputSteamToString(input));
        if(!jsonObject.has("type")) return result;
        CoordinatesHelper coordinatesHelper = getConverter(jsonObject);
        JSONObject geometry;
        String type = jsonObject.getString("type");
        if(type.equalsIgnoreCase("featurecollection")){
            JSONArray features = jsonObject.getJSONArray("features");
            for(int i = 0; i < features.length(); i++) {
                geometry = features.getJSONObject(i)
                        .getJSONObject("geometry");
                readGeometry(DataUtil.getRandomLandColor(context), geometry, coordinatesHelper, result);
            }
        }else if(type.equalsIgnoreCase("feature")){
            geometry = jsonObject.getJSONObject("geometry");
            readGeometry(DataUtil.getRandomLandColor(context), geometry, coordinatesHelper, result);
        }
        return result;
    }

    private static CoordinatesHelper getConverter(JSONObject root) throws JSONException {
        if(root.has("crs")){
            JSONObject crs = root.getJSONObject("crs");
            if(crs.has("properties")){
                JSONObject properties = crs.getJSONObject("properties");
                if(properties.has("name")){
                    String crs_name = properties.getString("name")
                            .replace("urn:ogc:def:crs:","")
                            .replace("OGC:1.3:","")
                            .replace("::",":")
                            .toUpperCase();
                    if(CoordinatesHelper.checkIfValid(crs_name)){
                        return new CoordinatesHelper(crs_name);
                    }
                }
            }
        }
        return null;
    }
    private static void readGeometry(ColorData color, JSONObject geometry, CoordinatesHelper coordinatesHelper, List<LandData> result) throws JSONException {
        String geometryType = geometry.getString("type").toLowerCase();
        if(geometryType.equals("polygon")) {
            LandData temp = readPolygon(color, geometry, coordinatesHelper);
            if(temp != null) result.add(temp);
        }else if(geometryType.equals("multipolygon")) {
            readMultiPolygon(color, geometry, coordinatesHelper, result);
        }
    }
    private static LandData readPolygon(ColorData color, JSONObject polygon, CoordinatesHelper coordinatesHelper) throws JSONException {
        JSONArray coordinatesArray = polygon.getJSONArray("coordinates");
        return getPolygon(color, coordinatesArray, coordinatesHelper);
    }
    private static void readMultiPolygon(ColorData color, JSONObject multiPolygon, CoordinatesHelper coordinatesHelper, List<LandData> result) throws JSONException {
        JSONArray coordinatesArray = multiPolygon.getJSONArray("coordinates");
        LandData temp;
        for(int i = 0; i < coordinatesArray.length(); i++){
            temp = getPolygon(color, coordinatesArray.getJSONArray(i), coordinatesHelper);
            if(temp != null) result.add(temp);
        }
    }
    private static LandData getPolygon(ColorData color, JSONArray jsonArray, CoordinatesHelper coordinatesHelper) throws JSONException {
        List<List<LatLng>> points  = new ArrayList<>();
        List<LatLng> temp;
        for(int i = 0; i < jsonArray.length(); i++){
            temp = new ArrayList<>();
            for(int j = 0; j < jsonArray.getJSONArray(i).length(); j++){
                if(jsonArray.getJSONArray(i).getJSONArray(j).length() > 1){
                    if(coordinatesHelper != null){
                        temp.add(coordinatesHelper.convertEPSG(
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(0),
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(1)
                        ));
                    }else{
                        temp.add(new LatLng(
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(1),
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(0)
                        ));
                    }
                }
            }
            points.add(temp);
        }
        if(points.size() == 1)
            return new LandData(
                    color,
                    points.get(0),
                    new ArrayList<>()
            );
        else if(points.size() > 1)
            return new LandData(
                    color,
                    points.get(0),
                    points.subList(1,points.size())
            );
        return null;
    }
}
