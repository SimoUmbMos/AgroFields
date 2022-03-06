package com.mosc.simo.ptuxiaki3741.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;

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
    private String tag;

    protected Land(Parcel in) {
        data = in.readParcelable(LandData.class.getClassLoader());
        selected = in.readByte() != 0;
        tag = in.readString();
    }
    public Land(String tag){
        this.data = null;
        this.selected=false;
        this.tag = tag;
    }
    public Land(LandData data){
        this.data = data;
        this.selected=false;
        this.tag = "";
    }

    public boolean isSelected(){
        return selected;
    }
    public String getTag() {
        return tag;
    }
    public LandData getData() {
        return data;
    }
    public LatLng getCenter(){
        if(data != null){
            if(data.getBorder().size()>0){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                data.getBorder().forEach(builder::include);
                return builder.build().getCenter();
            }
        }
        return null;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
    public void setTag(String tag) {
        this.tag = tag;
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
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(tag);
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

    @Override
    @NonNull
    public String toString() {
        String ans = "";
        if(data != null) {
            ans = data.toString();
        }else if(!tag.isEmpty()) {
            ans = tag;
        }
        return ans;
    }
}
