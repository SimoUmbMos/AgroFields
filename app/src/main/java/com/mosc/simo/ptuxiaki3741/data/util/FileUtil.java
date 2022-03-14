package com.mosc.simo.ptuxiaki3741.data.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.mosc.simo.ptuxiaki3741.backend.file.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.backend.file.gml.GMLExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.gml.GMLReader;
import com.mosc.simo.ptuxiaki3741.backend.file.kml.KmlExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.kml.KmlReader;
import com.mosc.simo.ptuxiaki3741.backend.file.openxml.OpenXmlDataBaseOutput;
import com.mosc.simo.ptuxiaki3741.backend.file.shapefile.MyShapeFileReader;
import com.mosc.simo.ptuxiaki3741.data.enums.FileType;
import com.mosc.simo.ptuxiaki3741.data.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.backend.file.wkt.WellKnownTextExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.wkt.WellKnownTextReader;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.nbsp.materialfilepicker.MaterialFilePicker;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    public static Intent getFilePickerIntent(Fragment fragment, LandFileState state, String title) {
        if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            if(state == LandFileState.Img){
                return new MaterialFilePicker()
                        .withSupportFragment(fragment)
                        .withCloseMenu(true)
                        .withFilter(Pattern.compile(".*\\.(jpg|jpeg|png)$"))
                        .withFilterDirectories(false)
                        .withHiddenFiles(false)
                        .withTitle(title)
                        .getIntent();
            }else{
                return new MaterialFilePicker()
                        .withSupportFragment(fragment)
                        .withCloseMenu(true)
                        .withFilter(Pattern.compile(".*\\.(geojson|json|kml|shp|xml|gml)$"))
                        .withFilterDirectories(false)
                        .withHiddenFiles(false)
                        .withTitle(title)
                        .getIntent();
            }
        }else{
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            if(state == LandFileState.Img){
                chooseFile.setType("image/*");
            }else{
                chooseFile.setType("*/*");
            }
            chooseFile = Intent.createChooser(chooseFile, title);
            return chooseFile;
        }

    }
    public static Intent getFilePickerIntent(Fragment fragment, String title) {
        if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            return new MaterialFilePicker()
                    .withSupportFragment(fragment)
                    .withCloseMenu(true)
                    .withFilter(Pattern.compile(".*\\.(xls|xlsx)$"))
                    .withFilterDirectories(false)
                    .withHiddenFiles(false)
                    .withTitle(title)
                    .getIntent();
        }else{
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, title);
            return chooseFile;
        }
    }
    public static boolean fileIsValidImg(String filePath){
        String extension = getExtension(filePath);
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg");
    }
    public static boolean fileIsValidImg(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx,response));
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg");
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
    public static boolean fileIsValid(String filePath){
        return
                isJSON(filePath) ||
                        isKML(filePath) ||
                        isGML(filePath) ||
                        isXML(filePath) ||
                        isText(filePath) ||
                        isShapeFile(filePath);
    }
    public static ArrayList<LandData> handleFile(Context ctx, Intent result){
        if(result != null){
            if(fileIsValid(ctx, result)){
                Uri uri = result.getData();
                ArrayList<LandData> data = new ArrayList<>();
                switch (getFileType(ctx, result)){
                    case KML:
                        try{
                            data.addAll(handleKml(ctx, uri));
                            return data;
                        }catch (Exception e){
                            Log.e(TAG, "handleKml: ", e);
                        }
                        break;
                    case SHAPEFILE:
                        try{
                            data.addAll(handleShapeFile(ctx, uri));
                            return data;
                        }catch (Exception e){
                            Log.e(TAG, "handleShapeFile: ", e);
                        }
                        break;
                    case GEOJSON:
                        try{
                            data.addAll(handleJson(ctx, uri));
                            return data;
                        }catch (Exception e){
                            Log.e(TAG, "handleJson: ", e);
                        }
                        break;
                    case GML:
                        try{
                            data.addAll(handleGML(ctx, uri));
                            return data;
                        }catch (Exception e){
                            Log.e(TAG, "handleGML: ", e);
                        }
                        break;
                    case XML:
                        try{
                            data.addAll(handleGML(ctx, uri));
                            if(data.size()>0){
                                return data;
                            }
                        }catch (Exception e){
                            Log.e(TAG, "handleGML: ", e);
                        }
                        data.clear();
                        try{
                            data.addAll(handleKml(ctx, uri));
                            if(data.size()>0){
                                return data;
                            }
                        }catch (Exception e){
                            Log.e(TAG, "handleKml: ", e);
                        }
                        break;
                    case TEXT:
                        try{
                            data.addAll(handleWKT(ctx, uri));
                            return data;
                        }catch (Exception e){
                            Log.e(TAG, "handleWKT: ", e);
                        }
                        break;
                }
            }
        }
        return new ArrayList<>();
    }
    public static ArrayList<LandData> handleFile(Context ctx, String filePath){
        if(fileIsValid(filePath)){
            File file = new File(filePath);
            ArrayList<LandData> data = new ArrayList<>();
            switch (getFileType(filePath)){
                case KML:
                    Log.d(TAG, "handleFile: KML");
                    try{
                        data.addAll(handleKml(ctx, new FileInputStream(file)));
                        return data;
                    }catch (Exception e){
                        Log.e(TAG, "handleKml: ", e);
                    }
                    break;
                case SHAPEFILE:
                    Log.d(TAG, "handleFile: SHAPEFILE");
                    try{
                        data.addAll(handleShapeFile(ctx, new FileInputStream(file)));
                        return data;
                    }catch (Exception e){
                        Log.e(TAG, "handleShapeFile: ", e);
                    }
                    break;
                case GEOJSON:
                    Log.d(TAG, "handleFile: GEOJSON");
                    try{
                        data.addAll(handleJson(ctx, new FileInputStream(file)));
                        return data;
                    }catch (Exception e){
                        Log.e(TAG, "handleJson: ", e);
                    }
                    break;
                case GML:
                    Log.d(TAG, "handleFile: GML");
                    try{
                        data.addAll(handleGML(ctx, new FileInputStream(file)));
                        return data;
                    }catch (Exception e){
                        Log.e(TAG, "handleGML: ", e);
                    }
                    break;
                case XML:
                    Log.d(TAG, "handleFile: XML");
                    try{
                        data.addAll(handleGML(ctx, new FileInputStream(file)));
                        if(data.size()>0){
                            return data;
                        }
                    }catch (Exception e){
                        Log.e(TAG, "handleGML: ", e);
                    }
                    data.clear();
                    try{
                        data.addAll(handleKml(ctx, new FileInputStream(file)));
                        if(data.size()>0){
                            return data;
                        }
                    }catch (Exception e){
                        Log.e(TAG, "handleKml: ", e);
                    }
                    break;
                case TEXT:
                    Log.d(TAG, "handleFile: TEXT");
                    try{
                        data.addAll(handleWKT(ctx, new FileInputStream(file)));
                        return data;
                    }catch (Exception e){
                        Log.e(TAG, "handleWKT: ", e);
                    }
                    break;
                default:
                    Log.d(TAG, "handleFile: non supported item");
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
    private static FileType getFileType(String filePath) {
        if(isKML(filePath))
            return FileType.KML;
        else if(isJSON(filePath))
            return FileType.GEOJSON;
        else if(isShapeFile(filePath))
            return FileType.SHAPEFILE;
        else if(isGML(filePath))
            return FileType.GML;
        else if(isText(filePath))
            return FileType.TEXT;
        else if(isXML(filePath))
            return FileType.XML;
        return FileType.NONE;
    }
    private static boolean isKML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("kml");
    }
    private static boolean isKML(String filePath){
        String extension = getExtension(filePath);
        return extension.equals("kml");
    }
    private static boolean isJSON(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("json") || extension.equals("geojson") ;
    }
    private static boolean isJSON(String filePath){
        String extension = getExtension(filePath);
        return extension.equals("json") || extension.equals("geojson") ;
    }
    private static boolean isShapeFile(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("shp");
    }
    private static boolean isShapeFile(String filePath){
        String extension = getExtension(filePath);
        return extension.equals("shp");
    }
    private static boolean isText(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("txt");
    }
    private static boolean isText(String filePath){
        String extension = getExtension(filePath);
        return extension.equals("txt");
    }
    private static boolean isXML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("xml");
    }
    private static boolean isXML(String filePath){
        String extension = getExtension(filePath);
        return extension.equals("xml");
    }
    private static boolean isGML(Context ctx, Intent response){
        String extension = getExtension(getFileName(ctx, response));
        return extension.equals("gml");
    }
    private static boolean isGML(String filePath){
        String extension = getExtension(filePath);
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
        return KmlReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleJson(Context ctx, Uri uri) throws Exception{
        return GeoJsonReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleGML(Context ctx, Uri uri) throws Exception{
        return GMLReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleWKT(Context ctx, Uri uri) throws Exception{
        return WellKnownTextReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleShapeFile(Context ctx, Uri uri) throws Exception{
        return MyShapeFileReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleKml(Context ctx, InputStream in) throws Exception{
        return KmlReader.exec(ctx, in);
    }
    private static ArrayList<LandData> handleJson(Context ctx, InputStream in) throws Exception{
        return GeoJsonReader.exec(ctx, in);
    }
    private static ArrayList<LandData> handleGML(Context ctx, InputStream in) throws Exception{
        return GMLReader.exec(ctx, in);
    }
    private static ArrayList<LandData> handleWKT(Context ctx, InputStream in){
        return WellKnownTextReader.exec(ctx, in);
    }
    private static ArrayList<LandData> handleShapeFile(Context ctx, InputStream in) throws Exception{
        return MyShapeFileReader.exec(ctx, in);
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
