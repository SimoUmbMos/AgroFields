package com.mosc.simo.ptuxiaki3741.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Lands")
public class Land {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "CreatorId")
    private long creator_id;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Lat")
    private double lat;
    @ColumnInfo(name = "Lng")
    private double lng;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(long creator_id) {
        this.creator_id = creator_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
