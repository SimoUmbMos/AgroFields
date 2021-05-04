package com.mosc.simo.ptuxiaki3741.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "LandPoints")
public class LandPoint {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Lid")
    private long lid;
    @ColumnInfo(name = "Position")
    private long position;
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

    public long getLid() {
        return lid;
    }

    public void setLid(long lid) {
        this.lid = lid;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
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
