package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;

@Entity(tableName = "LandPointsRecord")
public class LandPointRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandRecordID")
    private long landRecordID;
    @ColumnInfo(name = "Position")
    private long position;
    @ColumnInfo(name = "LatLng")
    private final LatLng latLng;

    @Ignore
    public LandPointRecord(long landRecordID, long position, LatLng latLng) {
        this.landRecordID = landRecordID;
        this.position = position;
        this.latLng = latLng;
    }

    public LandPointRecord(long id, long landRecordID, long position, LatLng latLng) {
        this.id = id;
        this.landRecordID = landRecordID;
        this.position = position;
        this.latLng = latLng;
    }

    @Ignore
    public LandPointRecord(LandDataRecord landRecord, LandPoint landPointRecord) {
        this.landRecordID = landRecord.getId();
        this.position = landPointRecord.getPosition();
        this.latLng = landPointRecord.getLatLng();
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

    public LatLng getLatLng() {
        return latLng;
    }
}
