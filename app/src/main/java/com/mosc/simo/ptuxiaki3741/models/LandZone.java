package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;

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
    private boolean admin;
    private boolean read;
    private boolean write;


    protected LandZone(Parcel in) {
        data = in.readParcelable(LandZoneData.class.getClassLoader());
        admin = in.readByte() != 0;
        read = in.readByte() != 0;
        write = in.readByte() != 0;
    }
    public LandZone(LandZoneData data, UserLandPermissions p){
        this.data = data;
        this.admin = p.isAdmin();
        this.read = p.isRead();
        this.write = p.isWrite();
    }

    public void setData(LandZoneData data) {
        this.data = data;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    public void setWrite(boolean write) {
        this.write = write;
    }

    public LandZoneData getData() {
        return data;
    }
    public boolean isAdmin() {
        return admin;
    }

    public boolean isRead() {
        return read;
    }
    public boolean isWrite() {
        return write;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(data,i);
        parcel.writeByte((byte) (admin ? 1 : 0));
        parcel.writeByte((byte) (read ? 1 : 0));
        parcel.writeByte((byte) (write ? 1 : 0));
    }
}
