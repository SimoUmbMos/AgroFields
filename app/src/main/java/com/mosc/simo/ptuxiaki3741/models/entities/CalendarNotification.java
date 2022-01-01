package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
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
public class CalendarNotification {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "LandID")
    private long lid;
    @ColumnInfo(name = "ZoneID")
    private long zid;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Message")
    private String message;
    @ColumnInfo(name = "Date")
    private Date date;

    public CalendarNotification(long id, long lid, long zid, String title, String message, Date date) {
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
    public long getLid() {
        return lid;
    }
    public long getZid() {
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
    public void setLid(long lid) {
        this.lid = lid;
    }
    public void setZid(long zid) {
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
}
