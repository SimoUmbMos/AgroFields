package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;

import java.util.ArrayList;
import java.util.List;

public class Land implements Parcelable {
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

    private LandData data;
    private final List<LandPoint> border;

    public static boolean equals(Land land1,Land land2){
        if(land1 == null && land2 == null){
            return true;
        }else if(land1 == null || land2 == null){
            return false;
        }else{
            LandData landData1 = land1.getData(),
                    landData2 = land2.getData();
            if(landData1 == null && landData2 == null){
                return true;
            }else if(landData1 == null || landData2 == null){
                return false;
            }else{
                return landData1.getId() == landData2.getId() ;
            }
        }
    }

    public Land(){
        this.data = null;
        this.border = new ArrayList<>();
    }
    public Land(LandData data){
        this.data = data;
        this.border = new ArrayList<>();
    }
    public Land(LandData data, List<LandPoint> border){
        this.data = data;
        this.border = new ArrayList<>(border);
    }

    protected Land(Parcel in) {
        data = in.readParcelable(LandData.class.getClassLoader());
        border = in.createTypedArrayList(LandPoint.CREATOR);
    }

    public LandData getData() {
        return data;
    }
    public List<LandPoint> getBorder() {
        return border;
    }

    public void setData(LandData data) {
        this.data = data;
    }
    public void setBorder(List<LandPoint> border) {
        this.border.clear();
        this.border.addAll(border);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
        dest.writeTypedList(border);
    }
}
