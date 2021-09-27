package com.mosc.simo.ptuxiaki3741.file.geojson.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Geometry {
    @Expose
    @SerializedName("coordinates")
    private Object coordinates;
    @Expose
    @SerializedName("type")
    private String type;

    public Object getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Object coordinates) {
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
