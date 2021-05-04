package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Crsproperties {
    @Expose
    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
