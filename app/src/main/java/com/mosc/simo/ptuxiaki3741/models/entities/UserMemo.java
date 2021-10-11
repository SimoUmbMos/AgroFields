package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserMemos")
public class UserMemo {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "UserID")
    long uid;
    @ColumnInfo(name = "ContactID")
    long cid;
    @ColumnInfo(name = "Memo")
    String memo;

    @Ignore
    public UserMemo(long uid, long cid, String memo) {
        this.uid = uid;
        this.cid = cid;
        this.memo = memo;
    }
    public UserMemo(long id, long uid, long cid, String memo) {
        this.id = id;
        this.uid = uid;
        this.cid = cid;
        this.memo = memo;
    }

    public long getId() {
        return id;
    }
    public long getUid() {
        return uid;
    }
    public long getCid() {
        return cid;
    }
    public String getMemo() {
        return memo;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
    public void setCid(long cid) {
        this.cid = cid;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
}
