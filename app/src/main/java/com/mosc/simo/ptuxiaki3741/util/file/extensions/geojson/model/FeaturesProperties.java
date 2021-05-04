package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeaturesProperties {
    @Expose
    @SerializedName("PER")
    private String PER;

    public String getPER() {
        return PER;
    }

    public void setPER(String PER) {
        this.PER = PER;
    }
}
