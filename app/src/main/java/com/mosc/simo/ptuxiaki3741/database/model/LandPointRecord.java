package com.mosc.simo.ptuxiaki3741.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "LandPointsRecord")
public class LandPointRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LRid")
    private long landRecordID;
    @ColumnInfo(name = "Position")
    private long position;
    @ColumnInfo(name = "Lat")
    private double lat;
    @ColumnInfo(name = "Lng")
    private double lng;

    @Ignore
    public LandPointRecord(long landRecordID, long position, double lat, double lng) {
        this.landRecordID = landRecordID;
        this.position = position;
        this.lat = lat;
        this.lng = lng;
    }

    public LandPointRecord(long id, long landRecordID, long position, double lat, double lng) {
        this.id = id;
        this.landRecordID = landRecordID;
        this.position = position;
        this.lat = lat;
        this.lng = lng;
    }
    @Ignore
    public LandPointRecord(LandRecord landRecord, LandPoint landPointRecord) {
        this.landRecordID = landRecord.getId();
        this.position = landPointRecord.getPosition();
        this.lat = landPointRecord.getLat();
        this.lng = landPointRecord.getLng();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLandRecordID() {
        return landRecordID;
    }

    public void setLandRecordID(long landRecordID) {
        this.landRecordID = landRecordID;
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
