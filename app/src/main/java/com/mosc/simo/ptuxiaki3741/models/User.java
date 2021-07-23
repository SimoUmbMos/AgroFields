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
    private String key;
    @ColumnInfo(name = "Username")
    private String username;
    @ColumnInfo(name = "Phone")
    private String phone;
    @ColumnInfo(name = "Email")
    private String email;

    public User(long id, String key, String username, String phone, String email) {
        this.id = id;
        this.key = key;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    public User(String key, String username, String phone, String email) {
        this.key = key;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    protected User(Parcel in) {
        id = in.readLong();
        key = in.readString();
        username = in.readString();
        phone = in.readString();
        email = in.readString();
    }

    public long getId() {
        return id;
    }
    public String getKey() {
        return key;
    }
    public String getUsername() {
        return username;
    }
    public String getPhone() {
        return phone;
    }
    public String getEmail() {
        return email;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setEmail(String email) {
        this.email = email;
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
        dest.writeString(phone);
        dest.writeString(email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.key, this.username, this.phone, this.email);
    }
}
