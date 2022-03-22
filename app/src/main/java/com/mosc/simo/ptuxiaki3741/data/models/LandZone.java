package com.mosc.simo.ptuxiaki3741.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;

public class LandZone implements Parcelable {
    public static final Creator<LandZone> CREATOR = new Creator<LandZone>() {
        @Override
        public LandZone createFromParcel(Parcel in) {
            return new LandZone(in);
        }

        @Override
        public LandZone[] newArray(int size) {
            return new LandZone[size];
        }
    };

    private LandZoneData data;
    private boolean selected;
    private String tag;

    protected LandZone(Parcel in) {
        data = in.readParcelable(LandZoneData.class.getClassLoader());
        selected = in.readByte() != 0;
        tag = in.readString();
    }
    public LandZone(String tag) {
        this.data = null;
        this.selected = false;
        this.tag = tag;
    }
    public LandZone(LandZoneData data){
        this.data = data;
        this.selected = false;
        this.tag = "";
    }

    public LandZone(LandZone that) {
        this.data = new LandZoneData(that.data);
        this.selected = that.selected;
        this.tag = that.tag;
    }

    public void setData(LandZoneData data) {
        this.data = data;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public LandZoneData getData() {
        return data;
    }
    public String getTag() {
        return tag;
    }
    public boolean isSelected() {
        return selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(data,i);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeString(tag);
    }

    @Override
    @NonNull
    public String toString() {
        if(data != null)
            return "#" + data.getId() + " " + data.getTitle();
        if(!tag.isEmpty())
            return tag;
        return "";
    }
}
