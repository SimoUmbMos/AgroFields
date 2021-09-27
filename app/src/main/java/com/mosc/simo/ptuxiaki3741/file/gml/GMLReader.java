package com.mosc.simo.ptuxiaki3741.file.gml;


import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

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
    public static List<Land> exec(InputStream is) {
        List<Land> border_fragment = new ArrayList<>();

        try {
            String s = convertStreamToString(is);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser  parser = factory.newPullParser();
            parser.setInput(new StringReader(s.replace("&","&amp;")));
            String tagName;
            Land temp;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if (tagName.equalsIgnoreCase("Polygon")){
                        temp = parsePolygon(parser);
                        if(temp != null)
                            border_fragment.add(temp);
                    }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return border_fragment;
    }

    private static Land parsePolygon(XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagName;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("outerBoundaryIs")){
                        return parserOuterBoundary(parser);
                    }else if(tagName.equalsIgnoreCase("exterior")){
                        return parserExterior(parser);
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

    private static Land parserOuterBoundary(XmlPullParser parser) throws XmlPullParserException, IOException{
        String tagName;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_TAG:
                    if(tagName.equalsIgnoreCase("coordinates")){
                        return parseCoordinates(parser);
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
    private static Land parserExterior(XmlPullParser parser) throws XmlPullParserException, IOException {
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
                        return parsePosList(parser,srsDimension);
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

    private static Land parseCoordinates(XmlPullParser parser) throws XmlPullParserException, IOException{
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
                    return new Land(new LandData(border));
                return null;
            }
            eventType = parser.next();
        }
        return null;}
    private static Land parsePosList(XmlPullParser parser,String dim) throws XmlPullParserException, IOException{
        List<LatLng> border = new ArrayList<>();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.TEXT){
                border.addAll(getBorderFromPosList(parser.getText(),dim));
            }else if(eventType == XmlPullParser.END_TAG){
                if(border.size()>0)
                    return new Land(new LandData(border));
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
            result.add(new LatLng(
                    Double.parseDouble(coord.split(",")[1]),
                    Double.parseDouble(coord.split(",")[0])
            ));
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
            result.add(new LatLng(
                    Double.parseDouble(coordPair.get(1)),
                    Double.parseDouble(coordPair.get(0))
            ));
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
