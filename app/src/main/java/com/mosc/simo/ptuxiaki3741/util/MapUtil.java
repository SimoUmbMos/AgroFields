package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MapUtil {
    public static final String TAG = "MapUtil";
    private MapUtil(){}

    public static Address findLocation(Context c, String a){
        if(a != null){
            String address = a.trim();
            if(!address.isEmpty()){
                try{
                    Geocoder geocoder = new Geocoder(c);
                    List<Address> locations = geocoder.getFromLocationName(a,1);
                    if(locations == null)
                        return null;
                    if(locations.size()>0)
                        return locations.get(0);
                }catch (Exception e){
                    Log.e(TAG, "findLocation: ", e);
                }
            }
        }
        return null;
    }
    public static Address findLocation(Context c, LatLng latLng){
        if(latLng != null){
            try{
                Geocoder geocoder = new Geocoder(c, Locale.getDefault());
                List<Address> locations =
                        geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                if(locations == null)
                    return null;
                if(locations.size()>0)
                    return locations.get(0);
            }catch (Exception e){
                Log.e(TAG, "findLocation: ", e);
            }
        }
        return null;
    }

    public static LatLng getPolygonCenter(List<LatLng> points){
        if(points != null){
            if(points.size() > 0){
                LatLng center;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(int i = 0 ; i < points.size() ; i++) {
                    builder.include(points.get(i));
                }
                LatLngBounds bounds = builder.build();
                center =  bounds.getCenter();

                return center;
            }
        }
        return null;
    }

    public static int closestPoint(List<LatLng> points,LatLng point) {
        double smallestDistance = -1, distance;
        int index = -1;
        for(int i = 0; i < points.size(); i++){
            distance  = distanceBetween(point,points.get(i));
            if(smallestDistance == -1 || distance < smallestDistance) {
                index = i;
                smallestDistance = distance;
            }
        }
        return index;
    }

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
                double area = SphericalUtil.computeArea(p);
                DecimalFormat df = new DecimalFormat("#.######");
                String areaString = df.format(area);
                return Double.parseDouble(areaString);
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
    public static double distanceBetween(LatLng p1, LatLng p2) {
        if(p1 != null && p2 != null){
            double lat1 = p1.latitude, lng1 = p1.longitude;
            double lat2 = p2.latitude, lng2 = p2.longitude;

            double earthRadius = 6371; // 3958.75 in miles, 6371 in kilometer

            double dLat = Math.toRadians(lat2-lat1);
            double dLng = Math.toRadians(lng2-lng1);

            double sindLat = Math.sin(dLat / 2);
            double sindLng = Math.sin(dLng / 2);

            double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                    * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            return earthRadius * c;
        }
        return Double.MAX_VALUE;
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

    public static List<List<LatLng>> difference(List<LatLng> p1, List<LatLng> p2) {
        List<List<LatLng>> ans = new ArrayList<>();

        com.esri.arcgisruntime.geometry.Polygon
                polygon1 = convert(p1),
                polygon2 = convert(p2);

        try{
            if(polygon1 != null & polygon2 != null){
                Geometry p3 = GeometryEngine.difference(polygon1,polygon2);
                JSONObject jsonObj = new JSONObject(p3.toJson());
                if(jsonObj.has("rings")){
                    JSONArray points;
                    List<LatLng> polygon3Borders;
                    for(int index = 0; index<jsonObj.getJSONArray("rings").length();index++){
                        points = jsonObj.getJSONArray("rings").getJSONArray(index);
                        polygon3Borders = new ArrayList<>();
                        for(int i=0;i<points.length()-1;i++){
                            polygon3Borders.add(new LatLng(points.getJSONArray(i).getDouble(0),points.getJSONArray(i).getDouble(1)));
                        }
                        ans.add(polygon3Borders);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ans;
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

    @Nullable private static com.esri.arcgisruntime.geometry.Polygon convert(List<LatLng> p){
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
