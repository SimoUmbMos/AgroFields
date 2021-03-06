package com.mosc.simo.ptuxiaki3741.backend.room.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "LandDataRecord")
public class LandDataRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "LandID")
    private final long landID;
    @ColumnInfo(name = "LandYear")
    private final long landYear;
    @ColumnInfo(name = "LandTitle")
    private final String landTitle;
    @ColumnInfo(name = "LandTags")
    private final String landTags;
    @ColumnInfo(name = "LandColor")
    private final ColorData landColor;
    @ColumnInfo(name = "ActionID")
    private final LandDBAction actionID;
    @ColumnInfo(name = "Date")
    private final Date date;
    @ColumnInfo(name = "LandBorder")
    private final List<LatLng> border;
    @ColumnInfo(name = "LandHoles")
    private final List<List<LatLng>> holes;

    @Ignore
    public LandDataRecord(LandData land, LandDBAction actionID, Date date) {
        this.id = 0;
        this.landYear = land.getYear();
        this.landID = land.getId();
        this.landTitle = land.getTitle();
        this.landTags = land.getTags();
        this.landColor = land.getColor();
        this.actionID = actionID;
        this.date = date;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(land.getBorder());
        setHoles(land.getHoles());
    }

    public LandDataRecord(long id, long landYear,
                          long landID, String landTitle, String landTags, ColorData landColor,
                          List<LatLng> border, List<List<LatLng>> holes,
                          LandDBAction actionID, Date date
    ) {
        this.id = id;
        this.landYear = landYear;
        this.landID = landID;
        this.landTitle = landTitle;
        this.landTags = landTags;
        this.landColor = landColor;
        this.actionID = actionID;
        this.date = date;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(border);
        setHoles(holes);
    }

    public long getId() {
        return id;
    }
    public long getLandID() {
        return landID;
    }
    public String getLandTitle() {
        return landTitle;
    }
    public String getLandTags() {
        return landTags;
    }
    public ColorData getLandColor() {
        return landColor;
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
    public List<List<LatLng>> getHoles() {
        return this.holes;
    }
    public long getLandYear() {
        return landYear;
    }

    public void setId(long id) {
        this.id = id;
    }
    private void setBorder(List<LatLng> border){
        this.border.clear();
        if(border != null)
            this.border.addAll(border);
    }
    private void setHoles(List<List<LatLng>> holes){
        this.holes.clear();
        this.holes.addAll(holes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandDataRecord that = (LandDataRecord) o;
        return
                id == that.id &&
                landYear == that.landYear &&
                landID == that.landID &&
                actionID == that.actionID &&
                landTitle.equals(that.landTitle) &&
                landTags.equals(that.landTags) &&
                date.equals(that.date) &&
                ListUtils.arraysMatch(border,that.border) &&
                ListUtils.arraysMatch(holes,that.holes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, landYear, landID, actionID, landTitle, landTags, date, border, holes);
    }
}
