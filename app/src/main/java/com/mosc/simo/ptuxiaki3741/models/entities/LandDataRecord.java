package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(
        tableName = "LandDataRecord",
        primaryKeys = {
            "ID",
            "Snapshot"
        },
        indices = {
                @Index(
                        value = {"ID","Snapshot"},
                        unique = true
                )
        }
)
public class LandDataRecord {
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "Snapshot")
    private long snapshot;
    @ColumnInfo(name = "LandID")
    private final long landID;
    @ColumnInfo(name = "LandTitle")
    private final String landTitle;
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
        this.snapshot = land.getSnapshot();
        this.landID = land.getId();
        this.landTitle = land.getTitle();
        this.landColor = land.getColor();
        this.actionID = actionID;
        this.date = date;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(land.getBorder());
        setHoles(land.getHoles());
    }

    public LandDataRecord(long id, long snapshot,
                          long landID, String landTitle, ColorData landColor,
                          List<LatLng> border, List<List<LatLng>> holes,
                          LandDBAction actionID, Date date
    ) {
        this.id = id;
        this.snapshot = snapshot;
        this.landID = landID;
        this.landTitle = landTitle;
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
    public long getSnapshot() {
        return snapshot;
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
    public void setSnapshot(long snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandDataRecord that = (LandDataRecord) o;
        return
                id == that.id &&
                snapshot == that.snapshot &&
                landID == that.landID &&
                actionID == that.actionID &&
                landTitle.equals(that.landTitle) &&
                date.equals(that.date) &&
                ListUtils.arraysMatch(border,that.border) &&
                ListUtils.arraysMatch(holes,that.holes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, snapshot, landID, actionID, landTitle, date, border,holes);
    }
}
