package com.mosc.simo.ptuxiaki3741.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Key")
    private int key;
    @ColumnInfo(name = "Username")
    private String username;

    public User(long id, int key, String username) {
        this.id = id;
        this.key = key;
        this.username = username;
    }
    @Ignore
    public User(int key, String username) {
        this.key = key;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
