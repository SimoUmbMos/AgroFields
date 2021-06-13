package com.mosc.simo.ptuxiaki3741.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "LandPoints")
public class LandPoint implements Parcelable,Comparable<LandPoint> {
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

    @Ignore
    public LandPoint(long lid, long position, LatLng latLng) {
        this.lid = lid;
        this.position = position;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
    }
    @Ignore
    public LandPoint(long id, long lid, long position, LatLng latLng) {
        this.id = id;
        this.lid = lid;
        this.position = position;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
    }

    public LandPoint(long id, long lid, long position, double lat, double lng) {
        this.id = id;
        this.lid = lid;
        this.position = position;
        this.lat = lat;
        this.lng = lng;
    }

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

    public static final Creator<LandPoint> CREATOR = new Creator<LandPoint>() {
        @Override
        public LandPoint createFromParcel(Parcel in) {
            return new LandPoint(in);
        }

        @Override
        public LandPoint[] newArray(int size) {
            return new LandPoint[size];
        }
    };
    @Ignore
    protected LandPoint(Parcel in) {
        id = in.readLong();
        lid = in.readLong();
        position = in.readLong();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(lid);
        dest.writeLong(position);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    @Override
    public int compareTo(LandPoint arg) {
        return Long.compare(this.position, arg.position);
    }

    public LatLng toLatLng() {
        return new LatLng(this.lat,this.lng);
    }
}
