package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson;

import java.util.ArrayList;
import java.util.List;

public class ExportGeoJsonFieldModel {
    private String title;
    private String key;
    private final List<List<Double>> points;

    public ExportGeoJsonFieldModel(String title,
                                   List<List<Double>> points
    ){
        this.title = title;
        this.key = randomStringWithSize(6)+"-"+
                randomStringWithSize(6)+"-"+
                randomStringWithSize(6)+"-"+
                randomStringWithSize(6);
        this.points= new ArrayList<>();
        this.points.addAll(points);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setPoints(List<List<Double>> points) {
        this.points.clear();
        this.points.addAll(points);
    }

    public String getTitle() {
        return title;
    }
    public String getKey() {
        return key;
    }
    public List<List<Double>> getPoints(){
        return points;
    }

    static String randomStringWithSize(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int)(AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }
}
