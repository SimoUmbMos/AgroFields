package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


    @Override
    public int hashCode() {
        if(this.getData() != null){
            return Objects.hash(this.getData().getId());
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else if (!(o instanceof Land)) {
            return false;
        } else {
            if(this.getData() == null && ((Land) o).getData() == null){
                return true;
            }else if(this.getData() != null && ((Land) o).getData() != null){
                return ((Land) o).getData().getId() == this.getData().getId();
            }else{
                return false;
            }
        }
    }
}
