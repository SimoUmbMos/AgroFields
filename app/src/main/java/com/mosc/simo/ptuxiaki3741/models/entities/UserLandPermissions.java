package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserLandPermissions")
public class UserLandPermissions implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long sid;
    @ColumnInfo(name = "UserID")
    private long userID;
    @ColumnInfo(name = "LandID")
    private long landID;
    @ColumnInfo(name = "AdminPermission")
    private boolean admin;
    @ColumnInfo(name = "ReadPermission")
    private boolean read;
    @ColumnInfo(name = "WritePermission")
    private boolean write;

    @Ignore
    public UserLandPermissions(long userID, long landID, boolean admin, boolean read, boolean write){
        this.userID = userID;
        this.landID = landID;
        this.admin = admin;
        if(admin){
            this.read = true;
            this.write = true;
        }else{
            this.read = read;
            this.write = write;
        }
    }
    @Ignore
    public UserLandPermissions(boolean admin, boolean read, boolean write){
        this.userID = -1;
        this.landID = -1;
        this.admin = admin;
        this.read = read;
        this.write = write;
    }
    public UserLandPermissions(long sid, long userID, long landID, boolean admin, boolean read, boolean write){
        this.sid = sid;
        this.userID = userID;
        this.landID = landID;
        this.admin = admin;
        if(admin){
            this.read = true;
            this.write = true;
        }else{
            this.read = read;
            this.write = write;
        }
    }

    public boolean hasPerms(){
        return isAdmin() || isRead() || isWrite();
    }

    public long getSid() {
        return sid;
    }
    public long getUserID() {
        return userID;
    }
    public long getLandID() {
        return landID;
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

    public void setSid(long sid) {
        this.sid = sid;
    }
    public void setUserID(long userID) {
        this.userID = userID;
    }
    public void setLandID(long landID) {
        this.landID = landID;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
        if(admin){
            this.read = true;
            this.write = true;
        }
    }
    public void setRead(boolean read) {
        if(!admin){
            this.read = read;
        }
    }
    public void setWrite(boolean write) {
        if(!admin){
            this.write = write;
        }
    }

    public static final Creator<UserLandPermissions> CREATOR = new Creator<UserLandPermissions>() {
        @Override
        public UserLandPermissions createFromParcel(Parcel in) {
            return new UserLandPermissions(in);
        }

        @Override
        public UserLandPermissions[] newArray(int size) {
            return new UserLandPermissions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    protected UserLandPermissions(Parcel in) {
        sid = in.readLong();
        userID = in.readLong();
        landID = in.readLong();
        admin = in.readByte() != 0;
        read = in.readByte() != 0;
        write = in.readByte() != 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(sid);
        parcel.writeLong(userID);
        parcel.writeLong(landID);
        parcel.writeByte((byte) (admin ? 1 : 0));
        parcel.writeByte((byte) (read ? 1 : 0));
        parcel.writeByte((byte) (write ? 1 : 0));
    }
}
