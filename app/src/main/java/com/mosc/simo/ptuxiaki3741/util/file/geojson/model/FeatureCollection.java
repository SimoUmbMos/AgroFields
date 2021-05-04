package com.mosc.simo.ptuxiaki3741.util.file.geojson.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeatureCollection {

    @Expose
    @SerializedName("crs")
    private Crs crs;
    @Expose
    @SerializedName("features")
    private List<Features> features;
    @Expose
    @SerializedName("totalFeatures")
    private int totalFeatures;
    @Expose
    @SerializedName("type")
    private String type;

    public Crs getCrs() {
        return crs;
    }

    public void setCrs(Crs crs) {
        this.crs = crs;
    }

    public List<Features> getFeatures() {
        return features;
    }

    public void setFeatures(List<Features> features) {
        this.features = features;
    }

    public int getTotalFeatures() {
        return totalFeatures;
    }

    public void setTotalFeatures(int totalFeatures) {
        this.totalFeatures = totalFeatures;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FeatureCollection(){

    }
}
