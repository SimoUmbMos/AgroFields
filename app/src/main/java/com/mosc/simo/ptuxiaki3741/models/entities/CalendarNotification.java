package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "CalendarNotification",
        indices = {
                @Index("id"),
                @Index("LandID"),
                @Index("ZoneID")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = LandData.class,
                        parentColumns = "id",
                        childColumns = "LandID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = LandZoneData.class,
                        parentColumns = "id",
                        childColumns = "ZoneID",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class CalendarNotification implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandID")
    private Long lid;
    @ColumnInfo(name = "ZoneID")
    private Long zid;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Message")
    private String message;
    @ColumnInfo(name = "Date")
    private Date date;

    @Ignore
    public CalendarNotification(Parcel in){
        id = in.readLong();
        lid = in.readLong();
        zid = in.readLong();
        title = in.readString();
        message = in.readString();
        date = (Date) in.readSerializable();

        if(lid == 0) lid = null;
        if(zid == 0) zid = null;
    }
    public CalendarNotification(long id, Long lid, Long zid, String title, String message, Date date) {
        setId(id);
        setLid(lid);
        setZid(zid);
        setTitle(title);
        setMessage(message);
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
    public Date getDate() {
        return date;
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
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(title);
        if(id != 0) {
            builder.append(" #");
            builder.append(id);
        }
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
        out.writeSerializable(date);
    }
}
