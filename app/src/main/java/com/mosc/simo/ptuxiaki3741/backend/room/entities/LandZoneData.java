package com.mosc.simo.ptuxiaki3741.backend.room.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;

import java.util.ArrayList;
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
    @ColumnInfo(name = "Tags")
    private String tags;
    @ColumnInfo(name = "Color")
    private ColorData color;
    @ColumnInfo(name = "Border")
    private List<LatLng> border;

    @Ignore
    public LandZoneData(LandZoneData in) {
        this(in.id,in.snapshot, in.lid, in.title, in.note, in.tags, new ColorData(in.color.toString()), in.border);
    }

    @Ignore
    protected LandZoneData(Parcel in) {
        id = in.readLong();
        snapshot = in.readLong();
        lid = in.readLong();
        title = in.readString();
        note = in.readString();
        tags = in.readString();
        color = in.readParcelable(ColorData.class.getClassLoader());
        border = in.createTypedArrayList(LatLng.CREATOR);
    }

    @Ignore
    public LandZoneData(long snapshot, long lid, String title, String note, String tags, ColorData color, List<LatLng> border) {
        this.id = 0;
        this.snapshot = snapshot;
        this.lid = lid;
        this.title = title;
        this.note = note;
        this.tags = tags;
        this.color = color;
        this.border = new ArrayList<>();
        setBorder(border);
    }

    public LandZoneData(long id, long snapshot, long lid, String title, String note, String tags, ColorData color, List<LatLng> border) {
        this.id = id;
        this.snapshot = snapshot;
        this.lid = lid;
        this.title = title;
        this.note = note;
        this.tags = tags;
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
    public String getTags() {
        return tags;
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
    public void setTags(String tags) {
        this.tags = tags;
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
        dest.writeString(tags);
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

    @Override
    @NonNull
    public String toString() {
        if(id > 0) return "#" + id + " " + title;
        return title;
    }
}
