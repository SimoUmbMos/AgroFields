package com.mosc.simo.ptuxiaki3741.backend.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(
        tableName = "LandZoneDataRecord",
        foreignKeys = {
                @ForeignKey(
                        entity = LandDataRecord.class,
                        parentColumns = {
                                "ID",
                                "Snapshot"
                        },
                        childColumns = {
                                "RecordID",
                                "RecordSnapshot"
                        },
                        onDelete = ForeignKey.CASCADE
                ),
        },
        indices = {
                @Index(
                        value = {"RecordID","RecordSnapshot"}
                ),
                @Index(
                        value = "ID",
                        unique = true
                )
        }
)
public class LandZoneDataRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "RecordID")
    private long recordID;
    @ColumnInfo(name = "RecordSnapshot")
    private long recordSnapshot;
    @ColumnInfo(name = "ZoneID")
    private long zoneID;
    @ColumnInfo(name = "ZoneTitle")
    private String zoneTitle;
    @ColumnInfo(name = "ZoneNote")
    private String zoneNote;
    @ColumnInfo(name = "ZoneColor")
    private ColorData zoneColor;
    @ColumnInfo(name = "ZoneBorder")
    private final List<LatLng> zoneBorder;

    @Ignore
    public LandZoneDataRecord(LandDataRecord record, LandZoneData zone) {
        this.recordID = record.getId();
        this.recordSnapshot = record.getSnapshot();
        this.zoneID = zone.getId();
        this.zoneTitle = zone.getTitle();
        this.zoneNote = zone.getNote();
        this.zoneColor = new ColorData(zone.getColor().toString());
        this.zoneBorder = new ArrayList<>(zone.getBorder());
    }

    public LandZoneDataRecord(
            long id, long recordID, long recordSnapshot,
            long zoneID, String zoneTitle, String zoneNote, ColorData zoneColor, List<LatLng> zoneBorder
    ) {
        this.id = id;
        this.recordID = recordID;
        this.recordSnapshot = recordSnapshot;
        this.zoneID = zoneID;
        this.zoneTitle = zoneTitle;
        this.zoneNote = zoneNote;
        this.zoneColor = zoneColor;
        this.zoneBorder = zoneBorder;
    }

    public long getId() {
        return id;
    }
    public long getRecordID() {
        return recordID;
    }
    public long getRecordSnapshot() {
        return recordSnapshot;
    }
    public long getZoneID() {
        return zoneID;
    }
    public String getZoneTitle() {
        return zoneTitle;
    }
    public String getZoneNote() {
        return zoneNote;
    }
    public ColorData getZoneColor() {
        return zoneColor;
    }
    public List<LatLng> getZoneBorder() {
        return zoneBorder;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setRecordID(long recordID) {
        this.recordID = recordID;
    }
    public void setRecordSnapshot(long recordSnapshot) {
        this.recordSnapshot = recordSnapshot;
    }
    public void setZoneID(long zoneID) {
        this.zoneID = zoneID;
    }
    public void setZoneTitle(String zoneTitle) {
        this.zoneTitle = zoneTitle;
    }
    public void setZoneNote(String zoneNote) {
        this.zoneNote = zoneNote;
    }
    public void setZoneColor(ColorData zoneColor) {
        this.zoneColor = zoneColor;
    }
    public void setZoneBorder(List<LatLng> zoneBorder) {
        this.zoneBorder.clear();
        if(zoneBorder != null){
            this.zoneBorder.addAll(zoneBorder);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandZoneDataRecord that = (LandZoneDataRecord) o;
        return id == that.id &&
                recordID == that.recordID &&
                recordSnapshot == that.recordSnapshot &&
                zoneID == that.zoneID &&
                zoneTitle.equals(that.zoneTitle) &&
                zoneNote.equals(that.zoneNote) &&
                zoneColor.equals(that.zoneColor) &&
                ListUtils.arraysMatch(zoneBorder,that.zoneBorder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recordID, recordSnapshot, zoneID, zoneTitle, zoneNote, zoneColor, zoneBorder);
    }
}
