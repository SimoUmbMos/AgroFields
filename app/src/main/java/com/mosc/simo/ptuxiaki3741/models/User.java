package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "Users")
public class User implements Parcelable {
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

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Key")
    private final String key;
    @ColumnInfo(name = "Username")
    private String username;

    public User(long id, String key, String username) {
        this.id = id;
        this.key = key;
        this.username = username;
    }
    @Ignore
    public User(String key, String username) {
        this.key = key;
        this.username = username;
    }
    @Ignore
    protected User(Parcel in) {
        id = in.readLong();
        key = in.readString();
        username = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(key);
        dest.writeString(username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.key, this.username);
    }
}
