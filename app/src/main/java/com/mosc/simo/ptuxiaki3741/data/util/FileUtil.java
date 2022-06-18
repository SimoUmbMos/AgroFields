package com.mosc.simo.ptuxiaki3741.data.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.mosc.simo.ptuxiaki3741.backend.file.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.geojson.GeoJsonReader;
import com.mosc.simo.ptuxiaki3741.backend.file.gml.GMLExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.gml.GMLReader;
import com.mosc.simo.ptuxiaki3741.backend.file.kml.KmlExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.kml.KmlReader;
import com.mosc.simo.ptuxiaki3741.backend.file.openxml.OpenXmlDataBaseOutput;
import com.mosc.simo.ptuxiaki3741.backend.file.openxml.OpenXmlState;
import com.mosc.simo.ptuxiaki3741.backend.file.shapefile.MyShapeFileReader;
import com.mosc.simo.ptuxiaki3741.data.enums.FileType;
import com.mosc.simo.ptuxiaki3741.data.enums.LandFileState;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    public static String landsToKmlString(List<Land> lands) {
        try{
            org.w3c.dom.Document document = KmlExporter.kmlFileExporter(lands);
            StringWriter writer = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (ParserConfigurationException | TransformerException e) {
            Log.e(TAG, "landsToKmlString: ", e);
        }
        return "";
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
    public static String zonesToKmlString(List<LandZone> zones) {
        List<Land> lands = new ArrayList<>();
        for(LandZone zone:zones){
            lands.add(new Land(new LandData(zone.getData().getBorder())));
        }
        return landsToKmlString(lands);
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
    public static Intent getFilePickerIntent(LandFileState state, String title) {
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
    public static Intent getFilePickerIntent(String title) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, title);
        return chooseFile;
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
                        isShapeFile(ctx, response);
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
        return KmlReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleJson(Context ctx, Uri uri) throws Exception{
        return GeoJsonReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleGML(Context ctx, Uri uri) throws Exception{
        return GMLReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static ArrayList<LandData> handleShapeFile(Context ctx, Uri uri) throws Exception{
        return MyShapeFileReader.exec(ctx, ctx.getContentResolver().openInputStream(uri));
    }
    private static String getExtension(String s){
        String[] segments = s.split("\\.");
        if(segments.length>0)
            return segments[segments.length-1];
        else
            return "";
    }

    public static boolean createFile(String content, OutputStream output){
        if(!content.isEmpty() && output != null){
            PrintWriter p = new PrintWriter(output);
            p.print(content);
            p.close();
            return true;
        }
        return false;
    }

    public static boolean dbExportAFileFileXLS(OpenXmlState state, OutputStream output) throws IOException {
        if(state == null) return false;
        if(output == null) return false;
        return OpenXmlDataBaseOutput.exportDBXLS(output, state);
    }

    public static boolean dbExportAFileFileXLSX(OpenXmlState state, OutputStream output) throws IOException {
        if(state == null) return false;
        if(output == null) return false;
        return OpenXmlDataBaseOutput.exportDBXLSX(output, state);
    }
}
