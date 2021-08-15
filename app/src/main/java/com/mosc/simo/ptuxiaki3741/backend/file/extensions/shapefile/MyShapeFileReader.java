package com.mosc.simo.ptuxiaki3741.backend.file.extensions.shapefile;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType.POLYGON;
import static org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType.POLYLINE;

public class MyShapeFileReader {
    public static final String TAG = "MyShapeFileReader";
    public static List<List<LatLng>> exec(InputStream is){
        List<List<LatLng>> result = new ArrayList<>();
        try {
            ValidationPreferences prefs = new ValidationPreferences();
            prefs.setMaxNumberOfPointsPerShape(16650);
            ShapeFileReader r = new ShapeFileReader(is, prefs);
            AbstractShape s;
            while ((s = r.next()) != null) {
                if(s.getShapeType() == POLYLINE){
                    for (int i = 0; i < ((PolylineShape) s).getNumberOfParts(); i++) {
                        readFromShape(result, ((PolylineShape) s).getPointsOfPart(i));
                    }
                }else if(s.getShapeType() == POLYGON){
                    for (int i = 0; i < ((PolygonShape) s).getNumberOfParts(); i++) {
                        readFromShape(result, ((PolygonShape) s).getPointsOfPart(i));
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG, "readShapeFileReader: ", e);
        }
        return result;
    }

    private static void readFromShape(List<List<LatLng>> result, PointData[] pointsOfPart) {
        List<LatLng> tempList = new ArrayList<>();
        for (PointData point : pointsOfPart) {
            tempList.add(new LatLng(point.getY(), point.getX()));
        }
        result.add(tempList);
    }
}
