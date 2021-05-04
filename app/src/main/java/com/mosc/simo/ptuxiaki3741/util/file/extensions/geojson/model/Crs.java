package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Crs {
    @Expose
    @SerializedName("properties")
    private Crsproperties crsproperties;
    @Expose
    @SerializedName("type")
    private String type;

    public Crsproperties getCrsproperties() {
        return crsproperties;
    }

    public void setCrsproperties(Crsproperties crsproperties) {
        this.crsproperties = crsproperties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
