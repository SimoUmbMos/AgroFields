package com.mosc.simo.ptuxiaki3741.file.geojson;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.file.geojson.helper.CoordinatesConverter;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonReader {
    private static final String TAG = "GeoJsonReader";
    private static CoordinatesConverter converter;

    public static ArrayList<LandData> exec(InputStream inputStream) throws Exception{
        List<List<List<LatLng>>> lands = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(slurp(inputStream));
        if(!jsonObject.has("type")){
            return new ArrayList<>();
        }
        initConverter(jsonObject);
        String type = jsonObject.getString("type");
        if(type.equalsIgnoreCase("featurecollection")){
            JSONArray features = jsonObject.getJSONArray("features");
            for(int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                readGeometry(geometry,lands);
            }
        }else if(type.equalsIgnoreCase("feature")){
            JSONObject geometry = jsonObject.getJSONObject("geometry");
            readGeometry(geometry,lands);
        }
        converter = null;

        ArrayList<LandData> ans = new ArrayList<>();
        for(List<List<LatLng>> land : lands){
            if(land.size()>0){
                List<LatLng> border = new ArrayList<>(land.get(0));
                List<List<LatLng>> holes = new ArrayList<>();
                for(int i = 1; i < land.size(); i++){
                    holes.add(land.get(i));
                }
                if(border.size()>0){
                    ans.add(new LandData(border,holes));
                }
            }
        }
        Log.d(TAG, "exec: read "+ans.size()+" entries");
        return ans;
    }

    private static String slurp(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }
    private static void initConverter(JSONObject root) throws JSONException{
        converter = null;
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
                    if(CoordinatesConverter.checkIfValid(crs_name)){
                        converter = new CoordinatesConverter(crs_name);
                    }
                }
            }
        }
    }

    private static void readGeometry(JSONObject geometry, List<List<List<LatLng>>> lands) throws JSONException{
        String geometryType = geometry.getString("type").toLowerCase();
        if(geometryType.equals("polygon")){
            readPolygon(lands,geometry);
        }else if(geometryType.equals("multipolygon")){
            readMultiPolygon(lands,geometry);
        }
    }

    private static void readMultiPolygon(List<List<List<LatLng>>> ans, JSONObject geometry) throws JSONException {
        JSONArray jsonArray = geometry.getJSONArray("coordinates");
        for(int i = 0; i < jsonArray.length(); i++){
            List<List<LatLng>> data = getPolygon(jsonArray.getJSONArray(i));
            if(data.size()>0) {
                ans.add(data);
            }
        }
    }

    private static void readPolygon(List<List<List<LatLng>>> ans, JSONObject geometry) throws JSONException {
        JSONArray jsonArray = geometry.getJSONArray("coordinates");
        if(jsonArray.length()>0){
            List<List<LatLng>> data = getPolygon(jsonArray);
            if(data.size()>0) {
                ans.add(data);
            }
        }
    }

    private static List<List<LatLng>> getPolygon(JSONArray jsonArray) throws JSONException {
        List<List<LatLng>> exportData  = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            exportData.add(i,new ArrayList<>());
            for(int j = 0; j < jsonArray.getJSONArray(i).length(); j++){
                if(jsonArray.getJSONArray(i).getJSONArray(j).length() > 1){
                    if(converter != null){
                        exportData.get(i).add(converter.convertEPSG(
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(0),
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(1)
                        ));
                    }else{
                        exportData.get(i).add(new LatLng(
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(1),
                                jsonArray.getJSONArray(i).getJSONArray(j).getDouble(0)
                        ));
                    }
                }
            }
        }
        return exportData;
    }
}
