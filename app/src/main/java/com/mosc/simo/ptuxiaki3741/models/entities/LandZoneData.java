package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.ColorData;

import java.util.List;

@Entity(tableName = "LandZoneData",
        indices = {
                @Index(
                        value = {"LandID", "Snapshot"}
                ),
                @Index(
                        value = {"ID","Snapshot"},
                        unique = true
                )
        },
        foreignKeys = {
                @ForeignKey(
                        entity = LandData.class,
                        parentColumns = {
                                "ID",
                                "Snapshot"
                        },
                        childColumns = {
                                "LandID",
                                "Snapshot"
                        },
                        onDelete = ForeignKey.CASCADE
                )
        },
        primaryKeys = {
            "ID",
            "Snapshot"
        }
)
public class LandZoneData implements Parcelable {
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "Snapshot")
    private long snapshot;
    @ColumnInfo(name = "LandID")
    private final long lid;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Note")
    private String note;
    @ColumnInfo(name = "Color")
    private ColorData color;
    @ColumnInfo(name = "Border")
    private List<LatLng> border;

    @Ignore
    protected LandZoneData(Parcel in) {
        id = in.readLong();
        snapshot = in.readLong();
        lid = in.readLong();
        title = in.readString();
        note = in.readString();
        color = in.readParcelable(ColorData.class.getClassLoader());
        border = in.createTypedArrayList(LatLng.CREATOR);
    }
    @Ignore
    public LandZoneData(List<LatLng> border) {
        this.id = 0;
        this.lid = 0;
        this.title = "";
        this.note = "";
        this.color = new ColorData(80, 20, 249);
        this.border = border;
        this.snapshot = -1;
    }
    @Ignore
    public LandZoneData(long lid, String title, String note, ColorData color, List<LatLng> border) {
        this.id = 0;
        this.lid = lid;
        this.title = title;
        this.note = note;
        this.color = color;
        this.border = border;
        this.snapshot = -1;
    }
    @Ignore
    public LandZoneData(long id, long lid, String title, String note, ColorData color, List<LatLng> border) {
        this.id = id;
        this.lid = lid;
        this.title = title;
        this.note = note;
        this.color = color;
        this.border = border;
        this.snapshot = -1;
    }
    public LandZoneData(long id, long snapshot, long lid, String title, String note, ColorData color, List<LatLng> border) {
        this.id = id;
        this.snapshot = snapshot;
        this.lid = lid;
        this.title = title;
        this.note = note;
        this.color = color;
        this.border = border;
    }

    public long getId() {
        return id;
    }
    public long getLid() {
        return lid;
    }
    public String getTitle() {
        return title;
    }
    public String getNote() {
        return note;
    }
    public ColorData getColor(){
        return color;
    }
    public List<LatLng> getBorder() {
        return border;
    }
    public long getSnapshot() {
        return snapshot;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public void setColor(ColorData color) {
        this.color = color;
    }
    public void setBorder(List<LatLng> border) {
        this.border = border;
    }
    public void setSnapshot(long snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(snapshot);
        dest.writeLong(lid);
        dest.writeString(title);
        dest.writeString(note);
        dest.writeParcelable(color,flags);
        dest.writeTypedList(border);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LandZoneData> CREATOR = new Creator<LandZoneData>() {
        @Override
        public LandZoneData createFromParcel(Parcel in) {
            return new LandZoneData(in);
        }

        @Override
        public LandZoneData[] newArray(int size) {
            return new LandZoneData[size];
        }
    };
}
