package com.mosc.simo.ptuxiaki3741.file.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.mosc.simo.ptuxiaki3741.file.geojson.helper.CoordinatesConverter;
import com.mosc.simo.ptuxiaki3741.file.geojson.model.FeatureCollection;
import com.mosc.simo.ptuxiaki3741.file.geojson.model.Features;
import com.mosc.simo.ptuxiaki3741.file.geojson.model.Geometry;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonReader {
    public static ArrayList<LandData> exec(InputStream inputStream) throws Exception{
        String json = getStringFromInputStream(inputStream);
        FeatureCollection featureCollection = new Gson().fromJson(json, FeatureCollection.class);
        return onFeatureCollectionGet(featureCollection);
    }

    private static String getStringFromInputStream(InputStream inputStream) throws Exception{
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuilder sb = new StringBuilder();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }
        return sb.toString();
    }

    private static ArrayList<LandData> onFeatureCollectionGet(FeatureCollection featureCollection) {
        return getListsForFeatureCollection(featureCollection);
    }

    private static ArrayList<LandData> getListsForFeatureCollection(FeatureCollection featureCollection) {
        ArrayList<LandData> result = new ArrayList<>();
        if (featureCollection != null) {
            if (featureCollection.getCrs() != null) {
                String epsgCode =
                        featureCollection.getCrs().getCrsproperties().getName().split("::")[1];
                if (!epsgCode.equals("4326")) {
                    CoordinatesConverter coordinatesConverter =
                            new CoordinatesConverter(epsgCode, "4326");
                    for (Features feature : featureCollection.getFeatures()) {
                        result.addAll(extractShape(feature.getGeometry(), coordinatesConverter));
                    }
                } else {
                    for (Features feature : featureCollection.getFeatures()) {
                        result.addAll(extractShape(feature.getGeometry()));
                    }
                }
            } else {
                for (Features feature : featureCollection.getFeatures()) {
                    result.addAll(extractShape(feature.getGeometry()));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<LandData> extractShape(Geometry geometry, CoordinatesConverter coordinatesConverter){
        List<LandData> result= new ArrayList<>();
        switch (geometry.getType()) {
            case "Polygon":
                result.add(toPolygon(
                        (List<List<List<Double>>>) geometry.getCoordinates(),
                        coordinatesConverter
                ));
                return result;
            case "MultiPolygon":
                result.addAll(toMultiPolygon(
                        (List<List<List<List<Double>>>>) geometry.getCoordinates(),
                        coordinatesConverter
                ));
                return result;
        }
        return new ArrayList<>();
    }
    @SuppressWarnings("unchecked")
    private static List<LandData> extractShape(Geometry geometry){
        List<LandData> result= new ArrayList<>();
        switch (geometry.getType()) {
            case "Polygon":
                result.add(toPolygon(
                        (List<List<List<Double>>>) geometry.getCoordinates()
                ));
                return result;
            case "MultiPolygon":
                result.addAll(toMultiPolygon(
                        (List<List<List<List<Double>>>>) geometry.getCoordinates()
                ));
                return result;
        }
        return new ArrayList<>();
    }

    private static LandData toPolygon(List<List<List<Double>>> polygon, CoordinatesConverter coordinatesConverter) {
        List<List<LatLng>> holes= new ArrayList<>();
        List<LatLng> border= new ArrayList<>();
        List<LatLng> hole= new ArrayList<>();
        for(int i = 0; i < polygon.size();i++){
            if(i != 0){
                hole.clear();
                for(List<Double> point :polygon.get(i)){
                    hole.add(coordinatesConverter.convertEPSG(point.get(1),point.get(0)));
                }
                holes.add(hole);
            }else{
                for(List<Double> point :polygon.get(i)){
                    border.add(coordinatesConverter.convertEPSG(point.get(1),point.get(0)));
                }
            }
        }
        return new LandData(border,holes);
    }
    private static LandData toPolygon(List<List<List<Double>>> polygon) {
        List<List<LatLng>> holes= new ArrayList<>();
        List<LatLng> border= new ArrayList<>();
        List<LatLng> hole= new ArrayList<>();
        for(int i = 0; i < polygon.size();i++){
            if(i != 0){
                hole.clear();
                for(List<Double> point :polygon.get(i)){
                    hole.add(new LatLng(point.get(1),point.get(0)));
                }
                holes.add(hole);
            }else{
                for(List<Double> point :polygon.get(i)){
                    border.add(new LatLng(point.get(1),point.get(0)));
                }
            }
        }
        return new LandData(border,holes);
    }
    private static List<LandData> toMultiPolygon(List<List<List<List<Double>>>> multiPolygon, CoordinatesConverter coordinatesConverter) {
        List<LandData> result= new ArrayList<>();
        for(List<List<List<Double>>> polygonList : multiPolygon){
            result.add(toPolygon(polygonList,coordinatesConverter));
        }
        return result;
    }
    private static List<LandData> toMultiPolygon(List<List<List<List<Double>>>> multiPolygon) {
        List<LandData> result= new ArrayList<>();
        for(List<List<List<Double>>> polygonList : multiPolygon){
            result.add(toPolygon(polygonList));
        }
        return result;
    }
}
