package com.mosc.simo.ptuxiaki3741.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.file.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.file.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.file.gml.GMLExporter;
import com.mosc.simo.ptuxiaki3741.file.gml.GMLReader;
import com.mosc.simo.ptuxiaki3741.file.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.file.kml.KmlFileReader;
import com.mosc.simo.ptuxiaki3741.file.openxml.OpenXmlDataBaseOutput;
import com.mosc.simo.ptuxiaki3741.file.shapefile.MyShapeFileReader;
import com.mosc.simo.ptuxiaki3741.enums.FileType;
import com.mosc.simo.ptuxiaki3741.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.AppRepositoryImpl;

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
import java.net.URI;
import java.net.URISyntaxException;
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
                isKML(ctx, response) ||
                        isJSON(ctx, response) ||
                        isGML(ctx, response) ||
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
    private static ArrayList<LandData> handleKml(Context ctx, Uri uri) throws Exception{
        return KmlFileReader.exec(ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleJson(Context ctx, Uri uri) throws Exception{
        return GeoJsonReader.exec(ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleGML(Context ctx, Uri uri) throws Exception{
        return GMLReader.exec(ctx.getContentResolver().openInputStream(uri));
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

    public static boolean createDbExportAFileFileXLS( Context context ) throws IOException {
        if(context != null){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
            );
            String fileName = "export_"+(System.currentTimeMillis()/1000)+".xls";
            File mFile = new File(path, fileName);

            FileOutputStream outputStream = new FileOutputStream(mFile);
            return OpenXmlDataBaseOutput.exportDBXLS(
                    outputStream,
                    new AppRepositoryImpl(MainActivity.getRoomDb(context))
            );
        }
        return false;
    }

    public static boolean createDbExportAFileFileXLSX( Context context ) throws IOException {
        if(context != null){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
            );
            String fileName = "export_"+(System.currentTimeMillis()/1000)+".xlsx";
            File mFile = new File(path, fileName);

            FileOutputStream outputStream = new FileOutputStream(mFile);
            return OpenXmlDataBaseOutput.exportDBXLSX(
                    outputStream,
                    new AppRepositoryImpl(MainActivity.getRoomDb(context))
            );
        }
        return false;
    }
}
