package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "LandData")
public class LandData implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "CreatorID")
    private long creator_id;
    @ColumnInfo(name = "Title")
    private String title;

    @Ignore
    protected LandData(Parcel in) {
        id = in.readLong();
        creator_id = in.readLong();
        title = in.readString();
    }
    @Ignore
    public LandData(long creator_id, String title) {
        this.id = -1;
        this.creator_id = creator_id;
        this.title = title;
    }
    @Ignore
    public LandData(boolean setId,long creator_id, String title) {
        if(setId){
            this.id = -1;
        }
        this.creator_id = creator_id;
        this.title = title;
    }

    public LandData(long id, long creator_id, String title) {
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

    public static final Creator<LandData> CREATOR = new Creator<LandData>() {
        @Override
        public LandData createFromParcel(Parcel in) {
            return new LandData(in);
        }

        @Override
        public LandData[] newArray(int size) {
            return new LandData[size];
        }
    };

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

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.creator_id, this.title);
    }
}
