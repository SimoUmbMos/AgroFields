package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "LandHolePoints")
public class LandHolePoint implements Parcelable{
    public static final Creator<LandHolePoint> CREATOR = new Creator<LandHolePoint>() {
        @Override
        public LandHolePoint createFromParcel(Parcel in) {
            return new LandHolePoint(in);
        }

        @Override
        public LandHolePoint[] newArray(int size) {
            return new LandHolePoint[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Hid")
    private long hid;
    @ColumnInfo(name = "Position")
    private final long position;
    @ColumnInfo(name = "LatLng")
    private final LatLng latLng;

    public LandHolePoint(long id, long hid, long position, LatLng latLng){
        this.id = id;
        this.hid = hid;
        this.position = position;
        this.latLng = latLng;
    }
    @Ignore
    public LandHolePoint(long position, LatLng latLng){
        this.position = position;
        this.latLng = latLng;
    }
    @Ignore
    protected LandHolePoint(Parcel in) {
        id = in.readLong();
        hid = in.readLong();
        position = in.readLong();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public long getId() {
        return id;
    }
    public long getHid() {
        return hid;
    }
    public long getPosition() {
        return position;
    }
    public LatLng getLatLng() {
        return latLng;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setHid(long hid) {
        this.hid = hid;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(hid);
        dest.writeLong(position);
        dest.writeParcelable(latLng,flags);
    }
}
