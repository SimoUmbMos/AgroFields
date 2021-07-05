package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "LandPoints")
public class LandPoint implements Parcelable,Comparable<LandPoint>{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Lid")
    private long lid;
    @ColumnInfo(name = "Position")
    private long position;
    @ColumnInfo(name = "LatLng")
    private final LatLng latLng;

    @Ignore
    public LandPoint(long position, LatLng latLng) {
        this.position = position;
        this.latLng = latLng;
    }

    public LandPoint(long id, long lid, long position, LatLng latLng) {
        this.id = id;
        this.lid = lid;
        this.position = position;
        this.latLng = latLng;
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

    public LatLng getLatLng() {
        return latLng;
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
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        if(latLng != null)
            return latLng.describeContents();
        else
            return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(lid);
        dest.writeLong(position);
        dest.writeParcelable(latLng,flags);
    }

    @Override
    public int compareTo(LandPoint arg) {
        return Long.compare(this.position, arg.position);
    }
}
