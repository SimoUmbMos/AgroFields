package com.mosc.simo.ptuxiaki3741.helpers;

import com.google.android.gms.maps.model.LatLng;

import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;

public class CoordinatesHelper {
    private final BasicCoordinateTransform transform;

    public static boolean checkIfValid(String crs){
        try{
            CoordinateReferenceSystem test = new CRSFactory().createFromName(crs);
            return test != null;
        }catch (Exception e){
            return false;
        }
    }

    public CoordinatesHelper(String crs){
        CoordinateReferenceSystem srcCrs = new CRSFactory().createFromName(crs);
        CoordinateReferenceSystem dstCrs = new CRSFactory().createFromName("EPSG:4326");
        transform = new BasicCoordinateTransform(srcCrs, dstCrs);
    }

    public LatLng convertEPSG(double x, double y) {
        ProjCoordinate srcCoord = new ProjCoordinate(x, y);
        ProjCoordinate dstCoord = new ProjCoordinate();
        transform.transform(srcCoord, dstCoord);
        return new LatLng(dstCoord.y,dstCoord.x);
    }
}
