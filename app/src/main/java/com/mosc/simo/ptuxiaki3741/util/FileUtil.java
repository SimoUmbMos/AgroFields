package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import com.mosc.simo.ptuxiaki3741.file.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.file.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.file.gml.GMLExporter;
import com.mosc.simo.ptuxiaki3741.file.gml.GMLReader;
import com.mosc.simo.ptuxiaki3741.file.kml.KmlExporter;
import com.mosc.simo.ptuxiaki3741.file.kml.KmlReader;
import com.mosc.simo.ptuxiaki3741.file.openxml.OpenXmlDataBaseOutput;
import com.mosc.simo.ptuxiaki3741.file.shapefile.MyShapeFileReader;
import com.mosc.simo.ptuxiaki3741.enums.FileType;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.file.wkt.WellKnownTextExporter;
import com.mosc.simo.ptuxiaki3741.file.wkt.WellKnownTextReader;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {
    public static final String TAG = "FileUtil";
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
        Document document = KmlExporter.kmlFileExporter(label, lands);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        return xmOut.outputString(document);
    }
    public static String landsToWKTString(List<Land> lands) {
        return WellKnownTextExporter.WellKnownTextExport(lands);
    }
    public static String landsToGeoJsonString(List<Land> lands) {
        JSONObject export = GeoJsonExporter.geoJsonExport(lands);
        return export.toString();
    }
    public static String landsToGmlString(List<Land> lands) {
        Document document = GMLExporter.exportList(lands);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        String result = xmOut.outputString(document);
        result = result.replace(" standalone=\"yes\"","");
        result = result.replace("xmlns:schemaLocation","xsi:schemaLocation");
        result = result.replace(
                "xsi:schemaLocation=\"http://www.opengis.net/gml http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/gml http://schemas.opengis.net/gml/2.1.2/feature.xsd\""
        );
        return result;
    }
    public static String zonesToKmlString(List<LandZone> zones, String label) {
        List<Land> lands = new ArrayList<>();
        for(LandZone zone:zones){
            lands.add(new Land(new LandData(zone.getData().getBorder())));
        }
        return landsToKmlString(lands,label);
    }
    public static String zonesToWKTString(List<LandZone> zones) {
        List<Land> lands = new ArrayList<>();
        for(LandZone zone:zones){
            lands.add(new Land(new LandData(zone.getData().getBorder())));
        }
        return landsToWKTString(lands);
    }
    public static String zonesToGeoJsonString(List<LandZone> zones) {
        List<Land> lands = new ArrayList<>();
        for(LandZone zone:zones){
            lands.add(new Land(new LandData(zone.getData().getBorder())));
        }
        return landsToGeoJsonString(lands);
    }
    public static String zonesToGmlString(List<LandZone> zones) {
        List<Land> lands = new ArrayList<>();
        for(LandZone zone:zones){
            lands.add(new Land(new LandData(zone.getData().getBorder())));
        }
        return landsToGmlString(lands);
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
    public static Intent getFilePickerIntent() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        return chooseFile;
    }
    public static boolean fileIsValidImg(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("png") || extension.equals("jpg");
    }
    public static boolean fileIsValid(Context ctx, Intent response){
        return
                isJSON(ctx, response) ||
                isKML(ctx, response) ||
                isGML(ctx, response) ||
                isXML(ctx, response) ||
                isText(ctx, response) ||
                isShapeFile(ctx, response);
    }
    public static ArrayList<LandData> handleFile(Context ctx, Intent result) throws Exception{
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
                        return handleGML(ctx, uri);
                    case XML:
                        ArrayList<LandData> dataXML = new ArrayList<>();
                        try{
                            dataXML.addAll(handleGML(ctx, uri));
                        }catch (Exception e){
                            Log.e(TAG, "handleWKT: ", e);
                            dataXML.clear();
                        }
                        if(dataXML.size()>0){
                            return dataXML;
                        }
                        try{
                            dataXML.addAll(handleKml(ctx, uri));
                        }catch (Exception e){
                            Log.e(TAG, "handleWKT: ", e);
                            dataXML.clear();
                        }
                        return dataXML;
                    case TEXT:
                        ArrayList<LandData> dataText = new ArrayList<>();
                        try{
                            dataText.addAll(handleWKT(ctx, uri));
                        }catch (Exception e){
                            Log.e(TAG, "handleWKT: ", e);
                            dataText.clear();
                        }
                        return dataText;
                }
            }
        }
        return new ArrayList<>();
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
        else if(isText(ctx, result))
            return FileType.TEXT;
        else if(isXML(ctx, result))
            return FileType.XML;
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
    private static boolean isText(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("txt");
    }
    private static boolean isXML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("xml");
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
    private static ArrayList<LandData> handleKml(Context ctx, Uri uri) throws Exception{
        return KmlReader.exec(ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleJson(Context ctx, Uri uri) throws Exception{
        return GeoJsonReader.exec(ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleGML(Context ctx, Uri uri) throws Exception{
        return GMLReader.exec(ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleWKT(Context ctx, Uri uri) throws Exception{
        return WellKnownTextReader.exec(ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleShapeFile(Context ctx, Uri uri) throws Exception{
        return MyShapeFileReader.exec(ctx.getContentResolver().openInputStream(uri));
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

    public static boolean dbExportAFileFileXLS(List<Land> lands, List<LandZone> zones) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
        );
        String fileName = "export_"+(System.currentTimeMillis()/1000)+".xls";
        File mFile = new File(path, fileName);

        FileOutputStream outputStream = new FileOutputStream(mFile);
        if(lands.size()>0){
            return OpenXmlDataBaseOutput.exportDBXLS(outputStream, lands, zones);
        }
        return false;
    }

    public static boolean dbExportAFileFileXLSX(List<Land> lands, List<LandZone> zones) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
        );
        String fileName = "export_"+(System.currentTimeMillis()/1000)+".xlsx";
        File mFile = new File(path, fileName);

        FileOutputStream outputStream = new FileOutputStream(mFile);
        return OpenXmlDataBaseOutput.exportDBXLSX(outputStream, lands, zones);
    }
}
