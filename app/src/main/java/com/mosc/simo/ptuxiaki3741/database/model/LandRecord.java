package com.mosc.simo.ptuxiaki3741.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "LandRecords")
public class LandRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandId")
    private long landID;
    @ColumnInfo(name = "UserId")
    private long userID;
    @ColumnInfo(name = "ActionID")
    private int actionID;
    @ColumnInfo(name = "Date")
    private Date date;

    /**
     * @param actionID Flag for action: 1 created, 2 edit, 3 restore, 4 delete
     */
    @Ignore
    public LandRecord(Land land, User user, int actionID,Date date) {
        this.landID = land.getId();
        this.userID = user.getId();
        this.actionID = actionID;
        this.date = date;
    }
    public LandRecord(long id,long landID,long userID,int actionID,Date date) {
        this.id = id;
        this.landID = landID;
        this.userID = userID;
        this.actionID = actionID;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLandID() {
        return landID;
    }

    public void setLandID(long landID) {
        this.landID = landID;
    }

    public void setLandID(Land land) {
        this.landID = land.getId();
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public void setUserID(User user) {
        this.userID = user.getId();
    }

    public int getActionID() {
        return actionID;
    }

    public void setActionID(int actionID) {
        this.actionID = actionID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
