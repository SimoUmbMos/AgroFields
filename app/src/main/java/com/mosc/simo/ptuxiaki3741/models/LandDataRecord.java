package com.mosc.simo.ptuxiaki3741.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;

import java.util.Date;

@Entity(tableName = "LandDataRecord")
public class LandDataRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandId")
    private long landID;
    @ColumnInfo(name = "CreatorId")
    private long landCreatorID;
    @ColumnInfo(name = "LandTitle")
    private String landTitle;
    @ColumnInfo(name = "UserId")
    private long userID;
    @ColumnInfo(name = "ActionID")
    private LandDBAction actionID;
    @ColumnInfo(name = "Date")
    private Date date;

    @Ignore
    public LandDataRecord(LandData land, User user, LandDBAction actionID, Date date) {
        this.landID = land.getId();
        this.landCreatorID = land.getCreator_id();
        this.landTitle = land.getTitle();
        this.userID = user.getId();
        this.actionID = actionID;
        this.date = date;
    }

    public LandDataRecord(long id, long landID, long landCreatorID, String landTitle, long userID, LandDBAction actionID, Date date) {
        this.id = id;
        this.landID = landID;
        this.landCreatorID = landCreatorID;
        this.landTitle = landTitle;
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

    public long getLandCreatorID() {
        return landCreatorID;
    }

    public void setLandCreatorID(long landCreatorID) {
        this.landCreatorID = landCreatorID;
    }

    public String getLandTitle() {
        return landTitle;
    }

    public void setLandTitle(String landTitle) {
        this.landTitle = landTitle;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public LandDBAction getActionID() {
        return actionID;
    }

    public void setActionID(LandDBAction actionID) {
        this.actionID = actionID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
