package com.mosc.simo.ptuxiaki3741.fragmentrelated.helper;

import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.User;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter.XMLOUTPUT;

public class FileHelper {
    public static String landsToKmlString(List<Land> lands, User currUser) {
        List<ExportFieldModel> exportFieldModels = new ArrayList<>();
        landToExportModel(lands, exportFieldModels);
        Document document = KmlFileExporter.kmlFileExporter(
                String.valueOf(currUser.hashCode()),
                exportFieldModels);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        return xmOut.outputString(document);
    }

    public static String landsToGeoJsonString(List<Land> lands) {
        List<ExportFieldModel> exportFieldModels = new ArrayList<>();
        landToExportModel(lands, exportFieldModels);
        JSONObject export = GeoJsonExporter.geoJsonExport(exportFieldModels);
        return export.toString();
    }

    private static void landToExportModel(List<Land> lands, List<ExportFieldModel> exportFieldModels) {
        List<Double> latLng = new ArrayList<>();
        List<List<Double>> points = new ArrayList<>();
        List<List<List<Double>>> points2 = new ArrayList<>();
        for(Land land : lands){
            points.clear();
            points2.clear();
            for(LandPoint landPoint: land.getLandPoints()){
                latLng.clear();
                latLng.add(landPoint.getLatLng().longitude);
                latLng.add(landPoint.getLatLng().latitude);
                points.add(new ArrayList<>(latLng));
            }
            points2.add(new ArrayList<>(points));
            exportFieldModels.add(new ExportFieldModel(
                    land.getLandData().getTitle(),
                    String.valueOf(land.getLandData().hashCode()),
                    points2
            ));
        }
    }
}
