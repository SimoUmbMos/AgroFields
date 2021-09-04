package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "LandDataRecord")
public class LandDataRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandID")
    private final long landID;
    @ColumnInfo(name = "CreatorID")
    private final long landCreatorID;
    @ColumnInfo(name = "UserID")
    private final long userID;
    @ColumnInfo(name = "LandTitle")
    private final String landTitle;
    @ColumnInfo(name = "ActionID")
    private final LandDBAction actionID;
    @ColumnInfo(name = "Date")
    private final Date date;
    @ColumnInfo(name = "LandBorder")
    private final List<LatLng> border;

    @Ignore
    public LandDataRecord(LandData land, User user, LandDBAction actionID, Date date) {
        this.landID = land.getId();
        this.landCreatorID = land.getCreator_id();
        this.landTitle = land.getTitle();
        this.border = new ArrayList<>(land.getBorder());
        this.userID = user.getId();
        this.actionID = actionID;
        this.date = date;
    }
    @Ignore
    public LandDataRecord(long id,LandData land, long userID, LandDBAction actionID, Date date) {
        this.id = id;
        this.landID = land.getId();
        this.landCreatorID = land.getCreator_id();
        this.landTitle = land.getTitle();
        this.border = new ArrayList<>(land.getBorder());
        this.userID = userID;
        this.actionID = actionID;
        this.date = date;
    }

    public LandDataRecord(long id, long landID, long landCreatorID, String landTitle, long userID, LandDBAction actionID, Date date,List<LatLng> border) {
        this.id = id;
        this.landID = landID;
        this.landCreatorID = landCreatorID;
        this.landTitle = landTitle;
        this.userID = userID;
        this.actionID = actionID;
        this.date = date;
        this.border = new ArrayList<>(border);
    }

    public long getId() {
        return id;
    }
    public long getLandID() {
        return landID;
    }
    public long getLandCreatorID() {
        return landCreatorID;
    }
    public String getLandTitle() {
        return landTitle;
    }
    public long getUserID() {
        return userID;
    }
    public LandDBAction getActionID() {
        return actionID;
    }
    public Date getDate() {
        return date;
    }
    public List<LatLng> getBorder() {
        return border;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandDataRecord that = (LandDataRecord) o;
        return
                id == that.id &&
                landID == that.landID &&
                landCreatorID == that.landCreatorID &&
                userID == that.userID &&
                actionID == that.actionID &&
                landTitle.equals(that.landTitle) &&
                date.equals(that.date) &&
                ListUtils.arraysMatch(border,that.border);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, landID, landCreatorID, userID, actionID, landTitle, date, border);
    }
}
