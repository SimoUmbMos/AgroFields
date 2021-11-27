package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;

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

    protected LandZone(Parcel in) {
        data = in.readParcelable(LandZoneData.class.getClassLoader());
        selected = in.readByte() != 0;
    }
    public LandZone(LandZoneData data){
        this.data = data;
        this.selected = false;
    }

    public void setData(LandZoneData data) {
        this.data = data;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public LandZoneData getData() {
        return data;
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
    }
}
