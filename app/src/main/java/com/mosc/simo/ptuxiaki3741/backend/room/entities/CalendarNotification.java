package com.mosc.simo.ptuxiaki3741.backend.room.entities;

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
import java.util.Objects;

@Entity(tableName = "CalendarNotification",
        foreignKeys = {
                @ForeignKey(
                        entity = LandData.class,
                        parentColumns = "ID",
                        childColumns = "LandID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = LandZoneData.class,
                        parentColumns = "ID",
                        childColumns = "ZoneID",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(
                        value = "LandID"
                ),
                @Index(
                        value = "ZoneID"
                )
        }
)
public class CalendarNotification implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "CategoryID")
    private long categoryID;
    @ColumnInfo(name = "Year")
    private long year;
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
        categoryID = in.readLong();
        year = in.readLong();
        lid = in.readLong();
        zid = in.readLong();
        title = in.readString();
        message = in.readString();
        date = (Date) in.readSerializable();

        if(lid == 0) lid = null;
        if(zid == 0) zid = null;
    }
    @Ignore
    public CalendarNotification(CalendarNotification that) {
        this.id = that.id;
        this.categoryID = that.categoryID;
        this.year = that.year;
        this.lid = that.lid;
        this.zid = that.zid;
        this.title = that.title;
        this.message = that.message;
        this.date = that.date;
    }
    public CalendarNotification(long id, long categoryID, long year, Long lid, Long zid, String title, String message, Date date) {
        setId(id);
        setCategoryID(categoryID);
        setYear(year);
        setLid(lid);
        setZid(zid);
        setTitle(title);
        setMessage(message);
        setDate(date);
    }

    public long getId() {
        return id;
    }
    public long getCategoryID() {
        return categoryID;
    }
    public long getYear() {
        return year;
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
    public void setCategoryID(long categoryID) {
        this.categoryID = categoryID;
    }
    public void setYear(long year) {
        this.year = year;
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
        out.writeLong(categoryID);
        out.writeLong(year);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarNotification that = (CalendarNotification) o;
        return id == that.id &&
                categoryID == that.categoryID &&
                year == that.year &&
                Objects.equals(lid, that.lid) &&
                Objects.equals(zid, that.zid) &&
                title.equals(that.title) &&
                message.equals(that.message) &&
                date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryID, year, lid, zid, title, message, date);
    }
}
