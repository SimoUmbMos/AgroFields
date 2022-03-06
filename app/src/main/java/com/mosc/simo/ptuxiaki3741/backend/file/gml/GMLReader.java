package com.mosc.simo.ptuxiaki3741.backend.file.gml;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.helpers.CoordinatesHelper;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
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
    public static CoordinatesHelper converter;

    public static ArrayList<LandData> exec(Context context,InputStream is) throws Exception{
        ArrayList<LandData> border_fragment = new ArrayList<>();

        String s = convertStreamToString(is);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser  parser = factory.newPullParser();
        parser.setInput(new StringReader(s.replace("&","&amp;")));
        String tagName, attributeValue;
        LandData temp;

        converter = null;
        String converterInitOn = "";

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType){
                case XmlPullParser.START_TAG:
                    tagName = parser.getName();
                    if(converter == null){
                        attributeValue = parser.getAttributeValue(null,"srsName");
                        if(attributeValue != null){
                            if(initConverter(attributeValue)){
                                converterInitOn = tagName;
                            }
                        }
                    }
                    if (tagName.equalsIgnoreCase("Polygon")){
                        temp = parsePolygon(context, parser);
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
        converter = null;
        return border_fragment;
    }

    private static boolean initConverter(String value){
        String code;
        String[] values = value.split("#");
        if(values.length>1){
            code = values[values.length-1];
            code = "EPSG:"+code;
            if(CoordinatesHelper.checkIfValid(code)){
                converter = new CoordinatesHelper(code);
                return true;
            }
        }
        values = value.split(":");
        if(values.length>1){
            code = values[values.length-1];
            code = "EPSG:"+code;
            if(CoordinatesHelper.checkIfValid(code)){
                converter = new CoordinatesHelper(code);
                return true;
            }
        }
        return false;
    }

    private static LandData parsePolygon(Context context, XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagName;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("outerBoundaryIs")){
                        return parserOuterBoundary(context, parser);
                    }else if(tagName.equalsIgnoreCase("exterior")){
                        return parserExterior(context, parser);
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

    private static LandData parserOuterBoundary(Context context, XmlPullParser parser) throws XmlPullParserException, IOException{
        String tagName;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("coordinates")){
                        return parseCoordinates(context, parser);
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
    private static LandData parserExterior(Context context, XmlPullParser parser) throws XmlPullParserException, IOException {
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
                        return parsePosList(context, parser,srsDimension);
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

    private static LandData parseCoordinates(Context context, XmlPullParser parser) throws XmlPullParserException, IOException{
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
                coordinates = parser.getText().trim()
                        .replace(decimal,".")
                        .replace(cs,",")
                        .replace(ts," ");
                border.addAll(getBorderFromCoordinates(coordinates));
            }else{
                if(border.size()>0)
                    return new LandData(DataUtil.getRandomLandColor(context),border);
                return null;
            }
            eventType = parser.next();
        }
        return null;}
    private static LandData parsePosList(Context context, XmlPullParser parser, String dim) throws XmlPullParserException, IOException{
        List<LatLng> border = new ArrayList<>();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.TEXT){
                border.addAll(getBorderFromPosList(parser.getText(),dim));
            }else if(eventType == XmlPullParser.END_TAG){
                if(border.size()>0)
                    return new LandData(DataUtil.getRandomLandColor(context),border);
                return null;
            }
            eventType = parser.next();
        }
        return null;
    }

    private static List<LatLng> getBorderFromCoordinates(String s){
        List<LatLng> result = new ArrayList<>();
        String[] coordinatePairs = s.split(" ");
        for (String coord : coordinatePairs) {
            if(converter != null){
                result.add(converter.convertEPSG(
                        Double.parseDouble(coord.split(",")[0]),
                        Double.parseDouble(coord.split(",")[1])
                ));
            }else{
                result.add(new LatLng(
                        Double.parseDouble(coord.split(",")[1]),
                        Double.parseDouble(coord.split(",")[0])
                ));
            }
        }
        return result;
    }
    private static List<LatLng> getBorderFromPosList(String s,String dim){
        List<LatLng> result = new ArrayList<>();
        int dimension = Integer.parseInt(dim);
        String[] coord = s.trim().split(" ");
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
