package com.mosc.simo.ptuxiaki3741.backend.file.shapefile;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineZShape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyShapeFileReader {
    public static ArrayList<LandData> exec(Context context, InputStream is) throws Exception{
        ArrayList<LandData> result = new ArrayList<>();
        ValidationPreferences prefs = new ValidationPreferences();
        prefs.setMaxNumberOfPointsPerShape(100000);
        ShapeFileReader r = new ShapeFileReader(is, prefs);

        AbstractShape s;
        while ((s = r.next()) != null) {
            switch (s.getShapeType() ){
                case POLYLINE_M:
                    for (int i = 0; i < ((PolylineMShape) s).getNumberOfParts(); i++) {
                        readFromShape(context, result, ((PolylineMShape) s).getPointsOfPart(i));
                    }
                    break;
                case POLYLINE_Z:
                    for (int i = 0; i < ((PolylineZShape) s).getNumberOfParts(); i++) {
                        readFromShape(context, result, ((PolylineZShape) s).getPointsOfPart(i));
                    }
                    break;
                case POLYLINE:
                    for (int i = 0; i < ((PolylineShape) s).getNumberOfParts(); i++) {
                        readFromShape(context, result, ((PolylineShape) s).getPointsOfPart(i));
                    }
                    break;
                case POLYGON:
                    for (int i = 0; i < ((PolygonShape) s).getNumberOfParts(); i++) {
                        readFromShape(context, result, ((PolygonShape) s).getPointsOfPart(i));
                    }
                    break;
                case POLYGON_M:
                    for (int i = 0; i < ((PolygonMShape) s).getNumberOfParts(); i++) {
                        readFromShape(context, result, ((PolygonMShape) s).getPointsOfPart(i));
                    }
                    break;
                case POLYGON_Z:
                    for (int i = 0; i < ((PolygonZShape) s).getNumberOfParts(); i++) {
                        readFromShape(context, result, ((PolygonZShape) s).getPointsOfPart(i));
                    }
                    break;
            }
        }
        return result;
    }

    private static void readFromShape(Context context, List<LandData> result, PointData[] pointsOfPart) {
        List<LatLng> tempList = new ArrayList<>();
        for (PointData point : pointsOfPart) {
            tempList.add(new LatLng(point.getY(), point.getX()));
        }
        result.add(new LandData(DataUtil.getRandomLandColor(context),tempList));
    }
}
