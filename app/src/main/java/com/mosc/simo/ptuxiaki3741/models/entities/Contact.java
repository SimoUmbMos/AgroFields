package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "Contacts")
public class Contact implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Username")
    private String username;
    @ColumnInfo(name = "Phone")
    private String phone;
    @ColumnInfo(name = "Email")
    private String email;

    public Contact(long id, String username, String phone, String email) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    public Contact(String username, String phone, String email) {
        this.username = username;
        this.phone = phone;
        this.email = email;
    }
    @Ignore
    public Contact(String username) {
        this.username = username;
        this.phone = "";
        this.email = "";
    }

    public long getId() {
        return id;
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
    public int hashCode() {
        return Objects.hash(id, username, phone, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return id == contact.id && username.equals(contact.username) && phone.equals(contact.phone) && email.equals(contact.email);
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(username);
        dest.writeString(phone);
        dest.writeString(email);
    }
    @Ignore
    protected Contact(Parcel in) {
        id = in.readLong();
        username = in.readString();
        phone = in.readString();
        email = in.readString();
    }
}
