package com.mosc.simo.ptuxiaki3741.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Lands")
public class Land implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "CreatorId")
    private long creator_id;
    @ColumnInfo(name = "Title")
    private String title;

    @Ignore
    public Land(long creator_id, String title) {
        this.creator_id = creator_id;
        this.title = title;
    }

    public Land(long id, long creator_id, String title) {
        this.id = id;
        this.creator_id = creator_id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(long creator_id) {
        this.creator_id = creator_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static final Creator<Land> CREATOR = new Creator<Land>() {
        @Override
        public Land createFromParcel(Parcel in) {
            return new Land(in);
        }

        @Override
        public Land[] newArray(int size) {
            return new Land[size];
        }
    };

    @Ignore
    protected Land(Parcel in) {
        id = in.readLong();
        creator_id = in.readLong();
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(creator_id);
        dest.writeString(title);
    }
}
