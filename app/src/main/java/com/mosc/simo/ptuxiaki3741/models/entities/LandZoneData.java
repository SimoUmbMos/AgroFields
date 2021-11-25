package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

@Entity(tableName = "LandZoneData")
public class LandZoneData implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandID")
    private long lid;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Border")
    private List<LatLng> border;

    @Ignore
    protected LandZoneData(Parcel in) {
        id = in.readLong();
        lid = in.readLong();
        title = in.readString();
        border = in.createTypedArrayList(LatLng.CREATOR);
    }
    @Ignore
    public LandZoneData(long lid, String title, List<LatLng> border) {
        this.lid = lid;
        this.title = title;
        this.border = border;
    }
    public LandZoneData(long id, long lid, String title, List<LatLng> border) {
        this.id = id;
        this.lid = lid;
        this.title = title;
        this.border = border;
    }

    public long getId() {
        return id;
    }
    public long getLid() {
        return lid;
    }
    public String getTitle() {
        return title;
    }
    public List<LatLng> getBorder() {
        return border;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setLid(long lid) {
        this.lid = lid;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setBorder(List<LatLng> border) {
        this.border = border;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(lid);
        dest.writeString(title);
        dest.writeTypedList(border);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LandZoneData> CREATOR = new Creator<LandZoneData>() {
        @Override
        public LandZoneData createFromParcel(Parcel in) {
            return new LandZoneData(in);
        }

        @Override
        public LandZoneData[] newArray(int size) {
            return new LandZoneData[size];
        }
    };
}
