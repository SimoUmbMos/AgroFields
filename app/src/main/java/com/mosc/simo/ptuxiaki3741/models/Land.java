package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

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
    private boolean selected;

    public Land(){
        this.data = null;
        this.selected=false;
    }
    public Land(LandData data){
        this.data = data;
        this.selected=false;
    }
    protected Land(Parcel in) {
        data = in.readParcelable(LandData.class.getClassLoader());
        selected = in.readInt() == 1;
    }

    public boolean isSelected(){
        return selected;
    }
    public LandData getData() {
        return data;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
    public void setData(LandData data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
        if(selected){
            dest.writeInt(1);
        }else{
            dest.writeInt(0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Land land = (Land) o;
        if(data == null && land.data == null)
            return true;
        if(data == null || land.data == null)
            return false;
        return data.equals(land.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
