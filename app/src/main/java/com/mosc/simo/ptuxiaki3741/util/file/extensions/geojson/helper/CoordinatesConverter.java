package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson.helper;

import com.google.android.gms.maps.model.LatLng;

import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;

public class CoordinatesConverter {
    private final BasicCoordinateTransform transform;

    public CoordinatesConverter(String code1,String code2){
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:" + code1);
        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:" + code2);
        transform = new BasicCoordinateTransform(srcCrs, dstCrs);
    }

    public LatLng convertEPSG(double x, double y) {
        ProjCoordinate srcCoord = new ProjCoordinate(x, y);
        ProjCoordinate dstCoord = new ProjCoordinate();
        transform.transform(srcCoord, dstCoord);
        return new LatLng(dstCoord.y,dstCoord.x);
    }
}
