package com.mosc.simo.ptuxiaki3741.backend.file.helper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExportFieldModel {
    private String title;
    private String key;
    private final List<List<List<Double>>> points;

    public ExportFieldModel(String title,
                            List<List<List<Double>>> points
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
                            List<List<List<Double>>> points
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
    public void setPointsList(List<List<List<Double>>> points) {
        this.points.clear();
        this.points.addAll(points);
    }

    public String getTitle() {
        return title;
    }
    public String getKey() {
        return key;
    }
    public List<List<List<Double>>> getPointsList(){
        return points;
    }

    private String getRandomHexString(){
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(0x1000000);
        return String.format("%06x", num);
    }
}
