package com.mosc.simo.ptuxiaki3741.backend.file.gml;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.helpers.CoordinatesHelper;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GMLReader {
    public static String TAG = "GMLReader";

    public static ArrayList<LandData> exec(Context context,InputStream is) throws Exception{
        ArrayList<LandData> border_fragment = new ArrayList<>();

        String s = convertStreamToString(is);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser  parser = factory.newPullParser();
        parser.setInput(new StringReader(s.replace("&","&amp;")));
        String tagName, attributeValue;
        LandData temp;

        CoordinatesHelper converter = null;
        String converterInitOn = "";

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType){
                case XmlPullParser.START_TAG:
                    tagName = parser.getName();
                    if(converter == null){
                        attributeValue = parser.getAttributeValue(null,"srsName");
                        if(attributeValue != null){
                            converter = initConverter(attributeValue);
                            if(converter != null){
                                converterInitOn = tagName;
                            }
                        }
                    }
                    if (tagName.equalsIgnoreCase("Polygon")){
                        temp = parsePolygon(converter, DataUtil.getRandomLandColor(context), parser);
                        if(temp != null) {
                            border_fragment.add(temp);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(converter != null){
                        tagName = parser.getName();
                        if (tagName.equals(converterInitOn)){
                            converter = null;
                            converterInitOn = "";
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return border_fragment;
    }

    private static CoordinatesHelper initConverter(String value){
        String code;
        String[] values = value.split("#");
        if(values.length>1){
            code = values[values.length-1];
            code = "EPSG:"+code;
            if(CoordinatesHelper.checkIfValid(code)){
                if(code.equals("EPSG:4326")) return null;
                return new CoordinatesHelper(code);
            }
        }
        values = value.split(":");
        if(values.length>1){
            code = values[values.length-1];
            code = "EPSG:"+code;
            if(CoordinatesHelper.checkIfValid(code)){
                if(code.equals("EPSG:4326")) return null;
                return new CoordinatesHelper(code);
            }
        }
        return null;
    }

    private static LandData parsePolygon(CoordinatesHelper converter, ColorData color, XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagName;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("outerBoundaryIs")){
                        return parserOuterBoundary(converter, color, parser);
                    }else if(tagName.equalsIgnoreCase("exterior")){
                        return parserExterior(converter, color, parser);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(tagName.equalsIgnoreCase("Polygon"))
                        return null;
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return null;
    }

    private static LandData parserOuterBoundary(CoordinatesHelper converter, ColorData color, XmlPullParser parser) throws XmlPullParserException, IOException{
        String tagName;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("coordinates")){
                        return parseCoordinates(converter, color, parser);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(tagName.equalsIgnoreCase("outerBoundaryIs")){
                        return null;
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return null;
    }
    private static LandData parserExterior(CoordinatesHelper converter, ColorData color, XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagName;
        String srsDimension="2";
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("LinearRing")){
                        for(int i = 0; i < parser.getAttributeCount();i++){
                            if(parser.getAttributeName(i).equalsIgnoreCase("srsDimension")){
                                srsDimension = parser.getAttributeValue(i);
                            }
                        }
                    }else if(tagName.equalsIgnoreCase("posList")){
                        return parsePosList(converter, color, parser,srsDimension);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(tagName.equalsIgnoreCase("exterior")){
                        return null;
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return null;
    }

    private static LandData parseCoordinates(CoordinatesHelper converter, ColorData color, XmlPullParser parser) throws XmlPullParserException, IOException{
        List<LatLng> border = new ArrayList<>();
        String cs = ",";
        String ts = " ";
        String decimal = ".";
        String coordinates;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG){
                for(int i = 0; i < parser.getAttributeCount(); i++){
                    if(parser.getAttributeName(i).equalsIgnoreCase("decimal")){
                        decimal = parser.getAttributeValue(i);
                    }else if(parser.getAttributeName(i).equalsIgnoreCase("cs")){
                        cs = parser.getAttributeValue(i);
                    }else if(parser.getAttributeName(i).equalsIgnoreCase("ts")){
                        ts = parser.getAttributeValue(i);
                    }
                }
            }else if(eventType == XmlPullParser.TEXT){
                coordinates = parser.getText()
                        .replace(decimal,".")
                        .replace(cs,",")
                        .replace(ts," ")
                        .trim()
                        .replaceAll("\n"," ")
                        .replaceAll("\t"," ")
                        .replaceAll(" +"," ");
                border.addAll(getBorderFromCoordinates(converter, coordinates));
            }else{
                if(border.size()>0)
                    return new LandData(color,border);
                return null;
            }
            eventType = parser.next();
        }
        return null;}
    private static LandData parsePosList(CoordinatesHelper converter, ColorData color, XmlPullParser parser, String dim) throws XmlPullParserException, IOException{
        List<LatLng> border = new ArrayList<>();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.TEXT){
                border.addAll(getBorderFromPosList(converter, parser.getText(), dim));
            }else if(eventType == XmlPullParser.END_TAG){
                if(border.size()>0)
                    return new LandData(color,border);
                return null;
            }
            eventType = parser.next();
        }
        return null;
    }

    private static List<LatLng> getBorderFromCoordinates(CoordinatesHelper converter, String s){
        List<LatLng> result = new ArrayList<>();
        String[] coordinatePairs = s.split(" ");
        for (String coord : coordinatePairs) {
            Log.d(TAG, "getBorderFromCoordinates: "+coord);
            String[] parts = coord.split(",");
            if(parts.length < 2) continue;
            if(converter != null){
                result.add(converter.convertEPSG(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1])
                ));
            }else{
                result.add(new LatLng(
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[0])
                ));
            }
        }
        return result;
    }
    private static List<LatLng> getBorderFromPosList(CoordinatesHelper converter, String s, String dim){
        List<LatLng> result = new ArrayList<>();
        int dimension = Integer.parseInt(dim);
        String[] coord = s.trim()
                .replaceAll("\n"," ")
                .replaceAll("\t"," ")
                .replaceAll(" +"," ")
                .split(" ");
        List<List<String>> coordPairs = new ArrayList<>();
        for(int i = dimension; i < coord.length; i = i+dimension){
            coordPairs.add(new ArrayList<>(Arrays.asList(coord).subList(i-dimension,i)));
        }
        for(List<String> coordPair : coordPairs){
            if(converter != null){
                result.add(converter.convertEPSG(
                        Double.parseDouble(coordPair.get(0)),
                        Double.parseDouble(coordPair.get(1))
                ));
            }else{
                result.add(new LatLng(
                        Double.parseDouble(coordPair.get(1)),
                        Double.parseDouble(coordPair.get(0))
                ));
            }
        }
        return result;
    }

    public static String convertStreamToString( InputStream is ) throws IOException {
        StringBuilder sb = new StringBuilder( Math.max( 16, is.available() ) );
        char[] tmp = new char[ 4096 ];
        try {
            InputStreamReader reader = new InputStreamReader( is, StandardCharsets.UTF_8 );
            for( int cnt; ( cnt = reader.read( tmp ) ) > 0; )
                sb.append( tmp, 0, cnt );
        } finally {
            is.close();
        }
        return sb.toString();
    }
}
