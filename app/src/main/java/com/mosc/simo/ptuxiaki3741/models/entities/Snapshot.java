package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity(
        tableName = "Snapshot",
        indices = {
                @Index(value = "Key")
        }
)
public class Snapshot implements Serializable {
    @PrimaryKey
    @ColumnInfo(name="Key")
    private long key;
    @ColumnInfo(name = "LandCount")
    private long landCount;
    @ColumnInfo(name = "ZoneCount")
    private long zoneCount;
    @ColumnInfo(name = "CalendarCount")
    private long calendarCount;
    @ColumnInfo(name = "RecordCount")
    private long recordCount;

    @Ignore
    private boolean needInit;

    @Ignore
    public Snapshot(long key) {
        this.key = key;

        needInit = true;
        landCount = 1;
        zoneCount = 1;
        calendarCount = 1;
        recordCount = 1;
    }

    public Snapshot(long key, long landCount, long zoneCount, long calendarCount, long recordCount) {
        this.key = key;
        needInit = false;
        this.landCount = landCount;
        this.zoneCount = zoneCount;
        this.calendarCount = calendarCount;
        this.recordCount = recordCount;
    }

    public long getKey() {
        return key;
    }

    public long getLandCount() {
        return landCount;
    }

    public long getZoneCount() {
        return zoneCount;
    }

    public long getCalendarCount() {
        return calendarCount;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public boolean needsInit(){
        return needInit;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public void setLandCount(long landCount) {
        this.landCount = landCount;
    }

    public void setZoneCount(long zoneCount) {
        this.zoneCount = zoneCount;
    }

    public void setCalendarCount(long calendarCount) {
        this.calendarCount = calendarCount;
    }

    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }

    public void setInit(boolean init){
        this.needInit = init;
    }


    public static Snapshot getInstance() {
        return new Snapshot(LocalDate.now().getYear());
    }

    public static Snapshot getInstance(long key) {
        return new Snapshot(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snapshot that = (Snapshot) o;
        return key == that.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(key);
    }
}
