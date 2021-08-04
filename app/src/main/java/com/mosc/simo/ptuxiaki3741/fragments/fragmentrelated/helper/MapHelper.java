package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.helper;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapHelper {

    public static List<LatLng> simplify(List<LatLng> p){
        if(p !=null){
            if(p.size()>2){
                if( p.get(0) != p.get(p.size()-1)){
                    p.add(p.get(0));
                }
                return PolyUtil.simplify(p, 10);
            }
        }
        return new ArrayList<>();
    }

    public static double area(List<LatLng> p){
        if(p != null){
            if(p.size()>2){
                if( p.get(0) != p.get(p.size()-1)){
                    p.add(p.get(0));
                }
                return SphericalUtil.computeArea(p);
            }
        }
        return 0;
    }

    public static double length(List<LatLng> p){
        if(p != null){
            if(p.size()>2){
                if( p.get(0) != p.get(p.size()-1)){
                    p.add(p.get(0));
                }
                return SphericalUtil.computeLength(p);
            }
        }
        return 0;
    }

    public static double distanceBetween(List<LatLng> p1, List<LatLng> p2) {

        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        if(polygon1 != null & polygon2 != null){
            return GeometryEngine.distanceBetween(polygon1,polygon2);
        }
        return -1;
    }

    public static boolean contains(LatLng point,List<LatLng> p){
        if(point != null && p !=null){
            return PolyUtil.containsLocation(point, p, true);
        }
        return false;
    }

    public static boolean disjoint(List<LatLng> p1, List<LatLng> p2) {

        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        if(polygon1 != null & polygon2 != null){
            return GeometryEngine.disjoint(polygon1,polygon2);
        }
        return false;
    }

    public static boolean equals(List<LatLng> p1, List<LatLng> p2) {
        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        if(polygon1 != null & polygon2 != null){
            return GeometryEngine.equals(polygon1,polygon2);
        }
        return false;
    }

    public static List<LatLng> union(List<LatLng> p1, List<LatLng> p2) {
        List<LatLng> polygon3Borders = new ArrayList<>();

        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        try{
            if(polygon1 != null & polygon2 != null){
                Geometry p3 = GeometryEngine.union(polygon1,polygon2);
                JSONObject jsonObj = new JSONObject(p3.toJson());
                JSONArray points = jsonObj.getJSONArray("rings").getJSONArray(0);
                for(int i=0;i<points.length()-1;i++){
                    polygon3Borders.add(new LatLng(points.getJSONArray(i).getDouble(0),points.getJSONArray(i).getDouble(1)));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return polygon3Borders;
    }

    public static List<LatLng> difference(List<LatLng> p1, List<LatLng> p2) {
        List<LatLng> polygon3Borders = new ArrayList<>();

        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        try{
            if(polygon1 != null & polygon2 != null){
                Geometry p3 = GeometryEngine.difference(polygon1,polygon2);
                JSONObject jsonObj = new JSONObject(p3.toJson());
                JSONArray points = jsonObj.getJSONArray("rings").getJSONArray(0);
                for(int i=0;i<points.length()-1;i++){
                    polygon3Borders.add(new LatLng(points.getJSONArray(i).getDouble(0),points.getJSONArray(i).getDouble(1)));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return polygon3Borders;
    }

    public static List<List<LatLng>> intersections(List<LatLng> p1, List<LatLng> p2) {
        List<List<LatLng>> intersectionsList = new ArrayList<>();
        List<LatLng> intersectionList = new ArrayList<>();

        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        try{
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
            e.printStackTrace();
        }
        return intersectionsList;
    }

    private static com.esri.arcgisruntime.geometry.Polygon convert(List<LatLng> p){
        PointCollection polygonPoints = new PointCollection(SpatialReferences.getWgs84());
        if(p !=null) {
            for(LatLng ll : p){
                polygonPoints.add(ll.latitude,ll.longitude);
            }
            return new com.esri.arcgisruntime.geometry.Polygon(polygonPoints);
        }
        return null;
    }
}
