package com.mosc.simo.ptuxiaki3741.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "LandOwners")
public class LandOwner {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Lid")
    private long lid;
    @ColumnInfo(name = "Uid")
    private long uid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLid() {
        return lid;
    }

    public void setLid(long lid) {
        this.lid = lid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
