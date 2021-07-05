package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Land implements Parcelable {
    private LandData landData;
    private final List<LandPoint> landPoints;

    public Land(){
        this.landData = null;
        this.landPoints = new ArrayList<>();
    }
    public Land(LandData landData){
        this.landData = landData;
        this.landPoints = new ArrayList<>();
    }
    public Land(LandData landData,List<LandPoint> landPoints){
        this.landData = landData;
        this.landPoints = new ArrayList<>(landPoints);
    }

    protected Land(Parcel in) {
        landData = in.readParcelable(LandData.class.getClassLoader());
        landPoints = in.createTypedArrayList(LandPoint.CREATOR);
    }

    public static final Creator<Land> CREATOR = new Creator<Land>() {
        @Override
        public Land createFromParcel(Parcel in) {
            return new Land(in);
        }

        @Override
        public Land[] newArray(int size) {
            return new Land[size];
        }
    };

    public LandData getLandData() {
        return landData;
    }
    public List<LandPoint> getLandPoints() {
        return landPoints;
    }

    public void setLandData(LandData landData) {
        this.landData = landData;
    }
    public void setLandPoints(List<LandPoint> landPoints) {
        this.landPoints.clear();
        this.landPoints.addAll(landPoints);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(landData, flags);
        dest.writeTypedList(landPoints);
    }
}
