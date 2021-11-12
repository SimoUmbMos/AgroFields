package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "Users")
public class User implements Parcelable,Cloneable {
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
    @ColumnInfo(name = "Username")
    private String username;
    @ColumnInfo(name = "Password")
    private String password;
    @ColumnInfo(name = "Phone")
    private String phone;
    @ColumnInfo(name = "Email")
    private String email;

    public User(long id, String username, String password, String phone, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    public User(String username, String password, String phone, String email) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    public User(String username, String phone, String email) {
        this.username = username;
        this.password = null;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.phone = null;
        this.email = null;
    }
    @Ignore
    public User(String username) {
        this.username = username;
        this.password = null;
        this.phone = null;
        this.email = null;
    }
    @Ignore
    protected User(Parcel in) {
        id = in.readLong();
        username = in.readString();
        password = in.readString();
        phone = in.readString();
        email = in.readString();
    }

    public long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
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
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String key) {
        this.password = key;
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
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(phone);
        dest.writeString(email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if(username == null && user.username == null )
            return true;
        if(username == null || user.username == null )
            return false;
        return id == user.id && username.equals(user.username);
    }

    @Override
    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
