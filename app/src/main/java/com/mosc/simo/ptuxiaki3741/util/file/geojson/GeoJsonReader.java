package com.mosc.simo.ptuxiaki3741.util.file.geojson;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.util.TaskRunner;
import com.mosc.simo.ptuxiaki3741.util.async.JsonToStringTask;
import com.mosc.simo.ptuxiaki3741.util.file.geojson.async.GeoJsonAsyncTask;
import com.mosc.simo.ptuxiaki3741.util.file.geojson.helper.CoordinatesConverter;
import com.mosc.simo.ptuxiaki3741.util.file.geojson.model.FeatureCollection;
import com.mosc.simo.ptuxiaki3741.util.file.geojson.model.Features;
import com.mosc.simo.ptuxiaki3741.util.file.geojson.model.Geometry;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonReader {
    public static final String TAG = "GeoJsonReader";

    public interface GeoJsonInterface{
        void onGeoJsonResult(List<List<LatLng>> listPoints);
    }

    public static void exec(InputStream inputStream,GeoJsonInterface geoJsonInterface){
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(new JsonToStringTask(inputStream), str ->
                stringToDTO(str,geoJsonInterface)
        );
    }

    private static void stringToDTO(String json,GeoJsonInterface geoJsonInterface){
        if(json!=null){
            Log.d(TAG, "stringToDTO: string not null");
            if(!json.equals("")){
                Log.d(TAG, "stringToDTO: string not empty");
                TaskRunner taskRunner = new TaskRunner();
                taskRunner.executeAsync(new GeoJsonAsyncTask(json), fc ->
                        onFeatureCollectionGet(fc,geoJsonInterface)
                );
            }
        }
    }

    private static void onFeatureCollectionGet(FeatureCollection featureCollection, GeoJsonInterface geoJsonInterface) {
        if(featureCollection != null){
            Log.d(TAG, "debug: FeatureCollection not empty");
            String epsgCode= featureCollection.getCrs().getCrsproperties().getName().split("::")[1];
            CoordinatesConverter coordinatesConverter = new CoordinatesConverter(epsgCode,"4326");
            List<List<LatLng>> result = new ArrayList<>();
            for(Features feature: featureCollection.getFeatures()){
                result.addAll(extractShape(feature.getGeometry(),coordinatesConverter));
            }
            geoJsonInterface.onGeoJsonResult(result);
        }
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
            Log.e(TAG, "extractShape: ", e);
        }
        return new ArrayList<>();
    }

    private static List<LatLng> toPolygon(List<List<List<Double>>> polygon, CoordinatesConverter coordinatesConverter) {
        List<LatLng> result= new ArrayList<>();
        for(List<List<Double>> points :polygon){
            for(List<Double> point :points){
                result.add(coordinatesConverter.convertEPSG(point.get(0),point.get(1)));
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
            result.add(coordinatesConverter.convertEPSG(point.get(0),point.get(1)));
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
        result.add(coordinatesConverter.convertEPSG(point.get(0),point.get(1)));
        return result;
    }
    private static List<List<LatLng>> toMultiPoint(List<List<Double>> multiPoint,CoordinatesConverter coordinatesConverter) {
        List<List<LatLng>> result= new ArrayList<>();
        for(List<Double> PointList : multiPoint){
            result.add(toPoint(PointList,coordinatesConverter));
        }
        return result;
    }
}
