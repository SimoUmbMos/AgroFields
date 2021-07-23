package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

public class LandHole implements Parcelable{
    public static final Creator<LandHole> CREATOR = new Creator<LandHole>() {
        @Override
        public LandHole createFromParcel(Parcel in) {
            return new LandHole(in);
        }

        @Override
        public LandHole[] newArray(int size) {
            return new LandHole[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Lid")
    private long lid;
    private final List<LandHolePoint> hole;

    public LandHole(long id, long lid, List<LandHolePoint> hole) {
        this.id = id;
        this.lid = lid;
        this.hole = new ArrayList<>(hole);
    }
    @Ignore
    public LandHole(long lid, List<LandHolePoint> hole) {
        this.lid = lid;
        this.hole = new ArrayList<>(hole);
    }
    @Ignore
    protected LandHole(Parcel in) {
        hole = in.createTypedArrayList(LandHolePoint.CREATOR);
    }

    public long getId() {
        return id;
    }
    public long getLid() {
        return lid;
    }
    public List<LandHolePoint> getHole() {
        return hole;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setLid(long lid) {
        this.lid = lid;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(hole);
    }
}
