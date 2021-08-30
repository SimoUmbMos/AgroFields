package com.mosc.simo.ptuxiaki3741.backend.file.helper;

import com.google.android.gms.maps.model.LatLng;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ExportFieldModel {
    private String title;
    private String key;
    private final List<List<LatLng>> points;

    public ExportFieldModel(String title,
                            List<List<LatLng>> points
    ){
        this.title = title;
        this.key = getRandomHexString()+"-"+
                getRandomHexString()+"-"+
                getRandomHexString()+"-"+
                getRandomHexString();
        this.points= new ArrayList<>();
        this.points.addAll(points);
    }
    public ExportFieldModel(String title,
                            String key,
                            List<List<LatLng>> points
    ){
        this.title = title;
        this.key = key;
        this.points= new ArrayList<>();
        this.points.addAll(points);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }
    public String getKey() {
        return key;
    }
    public List<List<LatLng>> getPointsList(){
        return points;
    }

    private String getRandomHexString(){
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(0x1000000);
        return String.format("%06x", num);
    }
}
