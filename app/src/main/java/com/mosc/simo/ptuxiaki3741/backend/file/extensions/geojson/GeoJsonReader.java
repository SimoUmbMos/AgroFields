package com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.helper.CoordinatesConverter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.model.FeatureCollection;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.model.Features;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.model.Geometry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonReader {
    public static List<List<LatLng>> exec(InputStream inputStream){
        String json = getStringFromInputStream(inputStream);
        FeatureCollection featureCollection = new Gson().fromJson(json, FeatureCollection.class);
        return onFeatureCollectionGet(featureCollection);
    }
    public static List<List<LatLng>> exec(String json){
        FeatureCollection featureCollection = new Gson().fromJson(json, FeatureCollection.class);
        return onFeatureCollectionGet(featureCollection);
    }

    private static String getStringFromInputStream(InputStream inputStream) {
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuilder sb = new StringBuilder();
        String str;
        try{
            while((str = reader.readLine())!= null){
                sb.append(str);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static List<List<LatLng>> onFeatureCollectionGet(FeatureCollection featureCollection) {
        return getListsForFeatureCollection(featureCollection);
    }

    private static List<List<LatLng>> getListsForFeatureCollection(FeatureCollection featureCollection) {
        List<List<LatLng>> result = new ArrayList<>();
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
    private static List<List<LatLng>> extractShape(Geometry geometry, CoordinatesConverter coordinatesConverter) {
        try {
            Object coordinates;
            List<List<LatLng>> result= new ArrayList<>();
            switch (geometry.getType()) {
                case "Polygon":
                    coordinates = geometry.getCoordinates();
                    List<List<List<Double>>> PolygonList =
                            (List<List<List<Double>>>) coordinates;
                    result.add(toPolygon(PolygonList,coordinatesConverter));
                    return result;
                case "MultiPolygon":
                    coordinates = geometry.getCoordinates();
                    List<List<List<List<Double>>>> MultiPolygonList =
                            (List<List<List<List<Double>>>>) coordinates;
                    return toMultiPolygon(MultiPolygonList,coordinatesConverter);
                case "LineString":
                    coordinates = geometry.getCoordinates();
                    List<List<Double>> LineStringList =
                            (List<List<Double>>) coordinates;
                    result.add(toLineString(LineStringList,coordinatesConverter));
                    return result;
                case "MultiLineString":
                    coordinates = geometry.getCoordinates();
                    List<List<List<Double>>> MultiLineStringList =
                            (List<List<List<Double>>>) coordinates;
                    return toMultiLineString(MultiLineStringList,coordinatesConverter);
                case "Point":
                    coordinates = geometry.getCoordinates();
                    List<Double> PointList =
                            (List<Double>) coordinates;
                    result.add(toPoint(PointList,coordinatesConverter));
                    return result;
                case "MultiPoint":
                    coordinates = geometry.getCoordinates();
                    List<List<Double>> MultiPointList =
                            (List<List<Double>>) coordinates;
                    return toMultiPoint(MultiPointList,coordinatesConverter);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    @SuppressWarnings("unchecked")
    private static List<List<LatLng>> extractShape(Geometry geometry) {
        try {
            Object coordinates;
            List<List<LatLng>> result= new ArrayList<>();
            switch (geometry.getType()) {
                case "Polygon":
                    coordinates = geometry.getCoordinates();
                    List<List<List<Double>>> PolygonList =
                            (List<List<List<Double>>>) coordinates;
                    result.add(toPolygon(PolygonList));
                    return result;
                case "MultiPolygon":
                    coordinates = geometry.getCoordinates();
                    List<List<List<List<Double>>>> MultiPolygonList =
                            (List<List<List<List<Double>>>>) coordinates;
                    return toMultiPolygon(MultiPolygonList);
                case "LineString":
                    coordinates = geometry.getCoordinates();
                    List<List<Double>> LineStringList =
                            (List<List<Double>>) coordinates;
                    result.add(toLineString(LineStringList));
                    return result;
                case "MultiLineString":
                    coordinates = geometry.getCoordinates();
                    List<List<List<Double>>> MultiLineStringList =
                            (List<List<List<Double>>>) coordinates;
                    return toMultiLineString(MultiLineStringList);
                case "Point":
                    coordinates = geometry.getCoordinates();
                    List<Double> PointList =
                            (List<Double>) coordinates;
                    result.add(toPoint(PointList));
                    return result;
                case "MultiPoint":
                    coordinates = geometry.getCoordinates();
                    List<List<Double>> MultiPointList =
                            (List<List<Double>>) coordinates;
                    return toMultiPoint(MultiPointList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<LatLng> toPolygon(List<List<List<Double>>> polygon, CoordinatesConverter coordinatesConverter) {
        List<LatLng> result= new ArrayList<>();
        for(List<List<Double>> points :polygon){
            for(List<Double> point :points){
                result.add(coordinatesConverter.convertEPSG(point.get(1),point.get(0)));
            }
        }
        return result;
    }
    private static List<List<LatLng>> toMultiPolygon(List<List<List<List<Double>>>> multiPolygon, CoordinatesConverter coordinatesConverter) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<List<List<Double>>> polygonList : multiPolygon){
            result.add(toPolygon(polygonList,coordinatesConverter));
        }
        return result;
    }
    private static List<LatLng> toLineString(List<List<Double>> lineString, CoordinatesConverter coordinatesConverter) {
        List<LatLng> result= new ArrayList<>();
        for(List<Double> point :lineString){
            result.add(coordinatesConverter.convertEPSG(point.get(1),point.get(0)));
        }
        return result;
    }
    private static List<List<LatLng>> toMultiLineString(List<List<List<Double>>> multiLineString, CoordinatesConverter coordinatesConverter) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<List<Double>> lineStringList : multiLineString){
            result.add(toLineString(lineStringList,coordinatesConverter));
        }
        return result;
    }
    private static List<LatLng> toPoint(List<Double> point,CoordinatesConverter coordinatesConverter) {
        List<LatLng> result= new ArrayList<>();
        result.add(coordinatesConverter.convertEPSG(point.get(1),point.get(0)));
        return result;
    }
    private static List<List<LatLng>> toMultiPoint(List<List<Double>> multiPoint,CoordinatesConverter coordinatesConverter) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<Double> PointList : multiPoint){
            result.add(toPoint(PointList,coordinatesConverter));
        }
        return result;
    }


    private static List<LatLng> toPolygon(List<List<List<Double>>> polygon) {
        List<LatLng> result= new ArrayList<>();
        for(List<List<Double>> points :polygon){
            for(List<Double> point :points){
                result.add(new LatLng(point.get(1),point.get(0)));
            }
        }
        return result;
    }
    private static List<List<LatLng>> toMultiPolygon(List<List<List<List<Double>>>> multiPolygon) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<List<List<Double>>> polygonList : multiPolygon){
            result.add(toPolygon(polygonList));
        }
        return result;
    }
    private static List<LatLng> toLineString(List<List<Double>> lineString) {
        List<LatLng> result= new ArrayList<>();
        for(List<Double> point :lineString){
            result.add(new LatLng(point.get(1),point.get(0)));
        }
        return result;
    }
    private static List<List<LatLng>> toMultiLineString(List<List<List<Double>>> multiLineString) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<List<Double>> lineStringList : multiLineString){
            result.add(toLineString(lineStringList));
        }
        return result;
    }
    private static List<LatLng> toPoint(List<Double> point) {
        List<LatLng> result= new ArrayList<>();
        result.add(new LatLng(point.get(1),point.get(0)));
        return result;
    }
    private static List<List<LatLng>> toMultiPoint(List<List<Double>> multiPoint) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<Double> PointList : multiPoint){
            result.add(toPoint(PointList));
        }
        return result;
    }

}
