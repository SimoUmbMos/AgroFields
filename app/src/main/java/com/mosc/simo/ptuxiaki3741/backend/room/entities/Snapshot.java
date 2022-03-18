package com.mosc.simo.ptuxiaki3741.backend.room.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
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
    @ColumnInfo(name = "RecordCount")
    private long recordCount;

    @Ignore
    public Snapshot(long key) {
        this.key = key;

        landCount = 1;
        zoneCount = 1;
        recordCount = 1;
    }

    public Snapshot(long key, long landCount, long zoneCount, long recordCount) {
        this.key = key;

        this.landCount = landCount;
        this.zoneCount = zoneCount;
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

    public long getRecordCount() {
        return recordCount;
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

    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
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
