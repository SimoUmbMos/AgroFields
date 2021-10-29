package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Embedded;
import androidx.room.Ignore;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;

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

    @Embedded
    private LandData data;
    @Embedded
    private UserLandPermissions perm;
    @Ignore
    private boolean selected;

    @Ignore
    protected Land(Parcel in) {
        data = in.readParcelable(LandData.class.getClassLoader());
        perm = in.readParcelable(UserLandPermissions.class.getClassLoader());
        selected = in.readByte() != 0;
    }
    @Ignore
    public Land(LandData data){
        this.data = data;
        this.perm = new UserLandPermissions(true,true,true);
        this.selected=false;
    }
    public Land(LandData data, UserLandPermissions perm){
        this.data = data;
        this.perm = perm;
        this.selected=false;
    }

    public boolean isSelected(){
        return selected;
    }
    public LandData getData() {
        return data;
    }
    public UserLandPermissions getPerm() {
        return perm;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
    public void setData(LandData data) {
        this.data = data;
    }
    public void setPerm(UserLandPermissions perm) {
        this.perm = perm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
        dest.writeParcelable(perm, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
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
