package com.mosc.simo.ptuxiaki3741.file.wkt;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WellKnownTextReader {

    public static ArrayList<LandData> exec(InputStream is){
        return exec(DataUtil.inputSteamToString(is));
    }

    public static ArrayList<LandData> exec(String is){
        ArrayList<LandData> result = new ArrayList<>();
        if(is != null){
            String in = is.toUpperCase().replaceAll("\\r\\n|\\r|\\n", " ").trim()
                    .replaceAll("\\)\\s+,","),")
                    .replaceAll(",\\s+\\(",",(")
                    .replaceAll("\\)\\s+\\)","))")
                    .replaceAll("\\(\\s+\\(","((")
                    .replaceAll(",\\s+", ",")
                    .replaceAll("\\s+,", ",")
                    .replaceAll("\\s+", " ");
            if(in.startsWith("POLYGON")){
                readPolygon(result, in);
            }else if(in.startsWith("TRIANGLE")){
                readPolygon(result, in);
            }else if(in.startsWith("MULTIPOLYGON")){
                readMultiPolygon(result, in);
            }else if(in.startsWith("GEOMETRYCOLLECTION")){
                readGeometryCollection(result, in);
            }
        }
        return result;
    }

    private static void readPolygon(ArrayList<LandData> result, String in) {
        List<List<LatLng>> land = new ArrayList<>();
        if(in.contains("(") || in.contains(")")){
            String data = in.substring(in.indexOf("(")+1,in.lastIndexOf(")")).trim();
            String[] pointsList = data.split("\\),\\(");
            for(int i = 0; i < pointsList.length; i++){
                land.add(i,new ArrayList<>());
                String points = pointsList[i].replace("(","").replace(")","").trim();
                String[] pointPairs = points.split(",");
                for (String pointPair : pointPairs) {
                    String[] pair = pointPair.trim().split(" ");
                    if (pair.length > 1) {
                        if(!pair[0].isEmpty() && !pair[1].isEmpty()){
                            land.get(i).add(new LatLng(
                                    Double.parseDouble(pair[0]),
                                    Double.parseDouble(pair[1])
                            ));
                        }
                    }
                }
            }
        }

        if(land.size()>0){
            List<LatLng> border = DataUtil.removeSamePointStartEnd(new ArrayList<>(land.get(0)));
            List<List<LatLng>> holes = new ArrayList<>();
            for(int i = 1; i < land.size(); i++){
                holes.add(DataUtil.removeSamePointStartEnd(new ArrayList<>(land.get(i))));
            }
            result.add(new LandData(border, holes));
        }
    }

    private static void readMultiPolygon(ArrayList<LandData> result, String in) {
        List<String> polygons = new ArrayList<>();
        if(in.contains("(") || in.contains(")")){
            String data = in.substring(in.indexOf("(")+1,in.lastIndexOf(")")).trim();
            polygons.addAll(readPolygons(data));
        }
        for(String polygon : polygons){
            readPolygon(result, polygon);
        }
    }

    private static void readGeometryCollection(ArrayList<LandData> result, String in) {
        //todo: code
    }

    private static List<String> readPolygons(String in){
        List<String> polygons = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int counter = 0;

        for(int i = 0 ; i < in.length(); i++){
            char c = in.charAt(i);
            if(counter==0 && c==','){
                polygons.add("POLYGON "+ current.toString());
                current = new StringBuilder();
                continue;
            }else if(c=='('){
                counter++;
            }else if(c==')'){
                counter--;
            }
            current.append(c);
        }
        if(current.length() > 0){
            polygons.add("POLYGON "+ current.toString());
        }

        return polygons;
    }

}