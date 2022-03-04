package com.mosc.simo.ptuxiaki3741.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;

public class ParcelableHole extends ArrayList<LatLng> implements Parcelable {
    public ParcelableHole(){
        super();
    }
    public ParcelableHole(Collection<LatLng> clone){
        super(clone);
    }
    protected ParcelableHole(Parcel in) {
        in.readTypedList(this, LatLng.CREATOR);
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this);
    }
    public static final Parcelable.Creator<ParcelableHole> CREATOR = new Parcelable.Creator<ParcelableHole>() {
        public ParcelableHole createFromParcel(Parcel in) {
            return new ParcelableHole(in);
        }
        public ParcelableHole[] newArray(int size) {
            return new ParcelableHole[size];
        }
    };
}
