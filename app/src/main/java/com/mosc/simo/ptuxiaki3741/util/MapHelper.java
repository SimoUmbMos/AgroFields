package com.mosc.simo.ptuxiaki3741.util;

import android.util.Log;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapHelper {
    private static final String TAG = "ERSIMapHelper";
    public static List<LatLng> intersection(Polygon p1, Polygon p2) {
        List<LatLng> polygon3Borders = new ArrayList<>();
        try{
            PointCollection polygonPoints = new PointCollection(SpatialReferences.getWgs84());
            com.esri.arcgisruntime.geometry.Polygon polygon1 = null, polygon2 = null;
            if(p1 !=null) {
                for(LatLng ll : p1.getPoints()){
                    polygonPoints.add(ll.latitude,ll.longitude);
                }
                polygon1 = new com.esri.arcgisruntime.geometry.Polygon(polygonPoints);
            }
            polygonPoints.clear();
            if(p2 !=null) {
                for(LatLng ll : p2.getPoints()){
                    polygonPoints.add(ll.latitude,ll.longitude);
                }
                polygon2 = new com.esri.arcgisruntime.geometry.Polygon(polygonPoints);
            }

            if(polygon1 != null & polygon2 != null){
                Geometry p3 = GeometryEngine.intersection(polygon1,polygon2);
                JSONObject jsonObj = new JSONObject(p3.toJson());
                JSONArray points = jsonObj.getJSONArray("rings").getJSONArray(0);
                for(int i=0;i<points.length()-1;i++){
                    polygon3Borders.add(new LatLng(points.getJSONArray(i).getDouble(0),points.getJSONArray(i).getDouble(1)));
                }
            }
        }catch (Exception e){
            Log.e(TAG, "intersection: ", e );
        }
        return polygon3Borders;
    }
    public static List<List<LatLng>> intersections(Polygon p1, Polygon p2) {
        List<List<LatLng>> intersectionsList = new ArrayList<>();
        List<LatLng> intersectionList = new ArrayList<>();
        try{
            PointCollection polygonPoints = new PointCollection(SpatialReferences.getWgs84());
            com.esri.arcgisruntime.geometry.Polygon polygon1 = null, polygon2 = null;
            if(p1 !=null) {
                for(LatLng ll : p1.getPoints()){
                    polygonPoints.add(ll.latitude,ll.longitude);
                }
                polygon1 = new com.esri.arcgisruntime.geometry.Polygon(polygonPoints);
            }
            polygonPoints.clear();
            if(p2 !=null) {
                for(LatLng ll : p2.getPoints()){
                    polygonPoints.add(ll.latitude,ll.longitude);
                }
                polygon2 = new com.esri.arcgisruntime.geometry.Polygon(polygonPoints);
            }

            if(polygon1 != null & polygon2 != null){
                List<Geometry> p3 = GeometryEngine.intersections(polygon1,polygon2);
                for(Geometry inter : p3) {
                    intersectionList.clear();
                    JSONObject jsonObj = new JSONObject(inter.toJson());
                    JSONArray points = jsonObj.getJSONArray("rings").getJSONArray(0);
                    for (int i = 0; i < points.length() - 1; i++) {
                        intersectionList.add(new LatLng(points.getJSONArray(i).getDouble(0), points.getJSONArray(i).getDouble(1)));
                    }
                    intersectionsList.add(intersectionList);
                }
            }
        }catch (Exception e){
            Log.e(TAG, "intersection: ", e );
        }
        return intersectionsList;
    }
}
