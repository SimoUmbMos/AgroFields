package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import com.mosc.simo.ptuxiaki3741.enums.CalendarEventType;

import java.util.Date;

@Entity(tableName = "CalendarNotification",
        indices = {
                @Index(
                        value = {"LandID", "Snapshot"}
                ),
                @Index(
                        value = {"ZoneID", "Snapshot"}
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
                ),
                @ForeignKey(
                        entity = LandZoneData.class,
                        parentColumns = {
                                "ID",
                                "Snapshot"
                        },
                        childColumns = {
                                "ZoneID",
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
public class CalendarNotification implements Parcelable {
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "Snapshot")
    private long snapshot;
    @ColumnInfo(name = "LandID")
    private Long lid;
    @ColumnInfo(name = "ZoneID")
    private Long zid;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Message")
    private String message;
    @ColumnInfo(name = "Type")
    private CalendarEventType type;
    @ColumnInfo(name = "Date")
    private Date date;

    @Ignore
    public CalendarNotification(Parcel in){
        id = in.readLong();
        snapshot = in.readLong();
        lid = in.readLong();
        zid = in.readLong();
        title = in.readString();
        message = in.readString();
        type = CalendarEventType.values()[in.readInt()];
        date = (Date) in.readSerializable();

        if(lid == 0) lid = null;
        if(zid == 0) zid = null;
    }
    public CalendarNotification(long id, long snapshot, Long lid, Long zid, String title, String message, CalendarEventType type, Date date) {
        setId(id);
        setSnapshot(snapshot);
        setLid(lid);
        setZid(zid);
        setTitle(title);
        setMessage(message);
        setType(type);
        setDate(date);
    }

    public long getId() {
        return id;
    }
    public Long getLid() {
        return lid;
    }
    public Long getZid() {
        return zid;
    }
    public String getTitle() {
        return title;
    }
    public String getMessage() {
        return message;
    }
    public CalendarEventType getType() {
        return type;
    }
    public Date getDate() {
        return date;
    }
    public long getSnapshot() {
        return snapshot;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setLid(Long lid) {
        this.lid = lid;
    }
    public void setZid(Long zid) {
        this.zid = zid;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setType(CalendarEventType type) {
        this.type = type;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setSnapshot(long snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(id != 0) {
            builder.append("#");
            builder.append(id);
            builder.append(" ");
        }
        builder.append(title);
        return builder.toString();
    }
    public static final Creator<CalendarNotification> CREATOR = new Creator<CalendarNotification>() {
        @Override
        public CalendarNotification createFromParcel(Parcel in) {
            return new CalendarNotification(in);
        }

        @Override
        public CalendarNotification[] newArray(int size) {
            return new CalendarNotification[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeLong(snapshot);
        if(lid == null){
            out.writeLong(0);
        }else{
            out.writeLong(lid);
        }
        if(zid == null){
            out.writeLong(0);
        }else{
            out.writeLong(zid);
        }
        out.writeString(title);
        out.writeString(message);
        out.writeInt(type.ordinal());
        out.writeSerializable(date);
    }
}
