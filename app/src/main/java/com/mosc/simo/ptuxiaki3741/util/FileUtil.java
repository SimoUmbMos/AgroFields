package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.ImportActivity;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileReader;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.shapefile.MyShapeFileReader;
import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;
import com.mosc.simo.ptuxiaki3741.enums.FileType;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml.KmlFileExporter.XMLOUTPUT;

public class FileUtil {
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
    public static Intent getFilePickerIntent(LandFileState state) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        if(state == LandFileState.Img){
            chooseFile.setType("image/*");
        }else{
            chooseFile.setType("*/*");
        }
        return chooseFile;
    }
    public static boolean fileIsValidImg(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("png") || extension.equals("jpg");
    }
    public static boolean fileIsValid(Context ctx, Intent response){
        return
                isKML(ctx, response) ||
                        isJSON(ctx, response) ||
                        isGML(ctx, response) ||
                        isShapeFile(ctx, response);
    }
    public static List<List<LatLng>> handleFile(Context ctx, Intent result) {
        if(fileIsValid(ctx, result)){
            Uri uri = result.getData();
            switch (getFileType(ctx, result)){
                case KML:
                    return handleKml(ctx, uri);
                case SHAPEFILE:
                    return handleShapeFile(ctx, uri);
                case GEOJSON:
                    return handleJson(ctx, uri);
                case GML:
                    return handleGML(ctx, uri);
            }
        }
        return new ArrayList<>();
    }
    public static Intent parseFile(Context ctx, Intent data) {
        Intent intent = null;
        if(fileIsValid(ctx, data)){
            intent = new Intent(ctx, ImportActivity.class);
            intent.setData(data.getData());
        }
        return intent;
    }

    private static FileType getFileType(Context ctx, Intent result) {
        if(isKML(ctx, result))
            return FileType.KML;
        else if(isJSON(ctx, result))
            return FileType.GEOJSON;
        else if(isShapeFile(ctx, result))
            return FileType.SHAPEFILE;
        else if(isGML(ctx, result))
            return FileType.GML;
        return FileType.NONE;
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
    private static boolean isKML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("kml");
    }
    private static boolean isJSON(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("json");
    }
    private static boolean isShapeFile(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("shp");
    }
    private static boolean isGML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("gml");
    }
    private static String getFileName(Context ctx, Intent response) {
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
    private static List<List<LatLng>> handleKml(Context ctx, Uri uri) {
        try{
            return KmlFileReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private static List<List<LatLng>> handleJson(Context ctx, Uri uri) {
        try{
            return GeoJsonReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<List<LatLng>> handleGML(Context ctx, Uri uri) {
        //todo handleGML
        return new ArrayList<>();
    }
    private static List<List<LatLng>> handleShapeFile(Context ctx, Uri uri) {
        try{
            return MyShapeFileReader.readShapeFileReader(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private static String getExtension(String s){
        String[] segments = s.split("\\.");
        if(segments.length>0)
            return segments[segments.length-1];
        else
            return "";
    }

}
