package com.mosc.simo.ptuxiaki3741.fragmentrelated.helper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileReader;
import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.User;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;

import java.io.File;
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
            for(LandPoint landPoint: land.getBorder()){
                latLng.clear();
                latLng.add(landPoint.getLatLng().longitude);
                latLng.add(landPoint.getLatLng().latitude);
                points.add(new ArrayList<>(latLng));
            }
            points2.add(new ArrayList<>(points));
            exportFieldModels.add(new ExportFieldModel(
                    land.getData().getTitle(),
                    String.valueOf(land.getData().hashCode()),
                    points2
            ));
        }
    }
    private Context ctx;
    public FileHelper(Context ctx){
        this.ctx = ctx;
    }

    private List<List<LatLng>> parseFile(Intent result) {
        if(fileIsValid(result)){
            return handleFile(result);
        }
        return new ArrayList<>();
    }
    public boolean fileIsValidImg(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("png") || extension.equals("jpg");
    }

    public boolean fileIsValid(Intent response){
        return isKML(response) || isJSON(response) || isGML(response);
    }
    public boolean isKML(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("kml");
    }
    public boolean isJSON(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("json");
    }
    public boolean isGML(Intent response){
        String extension = getExtension(getFileName(response));
        return extension.equals("gml");
    }
    private String getFileName(Intent response) {
        String displayName = "";
        Uri uri = response.getData();
        if(uri != null){
            String uriString = uri.toString();
            File myFile = new File(uriString);

            if (uriString.startsWith("content://")) {
                try (Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
        }
        return displayName;
    }

    public List<List<LatLng>> handleFile(Intent result) {
        if(fileIsValid(result)){
            Uri uri = result.getData();
            if(isKML(result)){
                return handleKml(uri);
            }else if(isJSON(result)){
                return handleJson(uri);
            }
        }
        return new ArrayList<>();
    }
    private List<List<LatLng>> handleKml(Uri uri) {
        try{
            return KmlFileReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private List<List<LatLng>> handleJson(Uri uri) {
        try{
            return GeoJsonReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private String getExtension(String s){
        String[] segments = s.split("\\.");
        if(segments.length>0)
            return segments[segments.length-1];
        else
            return "";
    }
}
