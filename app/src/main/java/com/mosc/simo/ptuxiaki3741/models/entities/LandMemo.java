package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "LandMemos")
public class LandMemo {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "LandID")
    long lid;
    @ColumnInfo(name = "UserID")
    long uid;
    @ColumnInfo(name = "Memo")
    String memo;

    @Ignore
    public LandMemo(long uid, long lid, String memo) {
        this.uid = uid;
        this.lid = lid;
        this.memo = memo;
    }
    public LandMemo(long id, long uid, long lid, String memo) {
        this.id = id;
        this.uid = uid;
        this.lid = lid;
        this.memo = memo;
    }

    public long getId() {
        return id;
    }
    public long getLid() {
        return lid;
    }
    public long getUid() {
        return uid;
    }
    public String getMemo() {
        return memo;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setLid(long lid) {
        this.lid = lid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
}
