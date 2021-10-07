package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.mosc.simo.ptuxiaki3741.ImportActivity;
import com.mosc.simo.ptuxiaki3741.file.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.file.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.file.gml.GMLExporter;
import com.mosc.simo.ptuxiaki3741.file.gml.GMLReader;
import com.mosc.simo.ptuxiaki3741.file.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.file.kml.KmlFileReader;
import com.mosc.simo.ptuxiaki3741.file.shapefile.MyShapeFileReader;
import com.mosc.simo.ptuxiaki3741.enums.FileType;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {
    public static final XMLOutputProcessor XMLOUTPUT = new AbstractXMLOutputProcessor() {
        @Override
        protected void printDeclaration(
                final Writer out,
                final FormatStack fStack
        ) throws IOException {
            write(out, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            write(out, fStack.getLineSeparator());
        }
    };
    private FileUtil(){}

    public static String landsToKmlString(List<Land> lands,String label) {
        Document document = KmlFileExporter.kmlFileExporter(label, lands);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        return xmOut.outputString(document);
    }
    public static String landsToGeoJsonString(List<Land> lands) {
        JSONObject export = GeoJsonExporter.geoJsonExport(lands);
        return export.toString();
    }
    public static String landsToGmlString(List<Land> lands) {
        Document document = GMLExporter.exportList(lands);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        String result = xmOut.outputString(document);
        result = result.replace("xmlns:schemaLocation","xsi:schemaLocation");
        result = result.replace(" standalone=\"yes\"","");
        return result;
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
    public static List<Land> handleFile(Context ctx, Intent result) {
        if(result != null){
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
                        List<Land> lands = handleGML(ctx, uri);
                        Log.d("debug", "handleFile gml size: "+lands.size());
                        return lands;
                }
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
    private static boolean isKML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("kml");
    }
    private static boolean isJSON(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("json") || extension.equals("geojson") ;
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
                        int value = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if( value >= 0){
                            displayName = cursor.getString(value);
                        }
                    }
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
        }
        return displayName;
    }
    private static List<Land> handleKml(Context ctx, Uri uri) {
        try{
            return KmlFileReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private static List<Land> handleJson(Context ctx, Uri uri) {
        try{
            return GeoJsonReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<Land> handleGML(Context ctx, Uri uri) {
        try{
            return GMLReader.exec(ctx.getContentResolver().openInputStream(uri));
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private static List<Land> handleShapeFile(Context ctx, Uri uri) {
        try{
            return MyShapeFileReader.exec(ctx.getContentResolver().openInputStream(uri));
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

    public static boolean createFile(String output, String fileName, File path) throws IOException {
        if(!output.isEmpty() && !fileName.isEmpty()){
            File mFile = new File(path, fileName);
            FileWriter writer = new FileWriter(mFile);
            writer.append(output);
            writer.flush();
            writer.close();
            return true;
        }
        return false;
    }
}
