package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "SharedLands")
public class SharedLand {
    @PrimaryKey(autoGenerate = true)
    private long sid;
    @ColumnInfo(name = "UserID")
    private long userID;
    @ColumnInfo(name = "LandID")
    private long landID;

    @Ignore
    public SharedLand(long userID, long landID){
        this.userID = userID;
        this.landID = landID;
    }
    public SharedLand(long sid, long userID, long landID){
        this.sid = sid;
        this.userID = userID;
        this.landID = landID;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getLandID() {
        return landID;
    }

    public void setLandID(long landID) {
        this.landID = landID;
    }
}
