package com.mosc.simo.ptuxiaki3741.util.file.extensions.geojson.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Features {
    @Expose
    @SerializedName("properties")
    private FeaturesProperties featuresProperties;
    @Expose
    @SerializedName("geometry_name")
    private String geometry_name;
    @Expose
    @SerializedName("geometry")
    private Geometry geometry;
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("type")
    private String type;

    public FeaturesProperties getFeaturesProperties() {
        return featuresProperties;
    }

    public void setFeaturesProperties(FeaturesProperties featuresProperties) {
        this.featuresProperties = featuresProperties;
    }

    public String getGeometry_name() {
        return geometry_name;
    }

    public void setGeometry_name(String geometry_name) {
        this.geometry_name = geometry_name;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
