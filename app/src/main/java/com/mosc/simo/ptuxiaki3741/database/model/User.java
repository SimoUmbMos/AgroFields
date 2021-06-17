package com.mosc.simo.ptuxiaki3741.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User implements Parcelable {
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
    @Ignore
    protected User(Parcel in) {
        id = in.readLong();
        key = in.readInt();
        username = in.readString();
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

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(key);
        dest.writeString(username);
    }
}
