package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "LandData")
public class LandData implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "CreatorID")
    private long creator_id;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Border")
    private final List<LatLng> border;

    @Ignore
    protected LandData(Parcel in) {
        id = in.readLong();
        creator_id = in.readLong();
        title = in.readString();
        border = in.createTypedArrayList(LatLng.CREATOR);
    }
    @Ignore
    public LandData(List<LatLng> border) {
        this.id = -1;
        this.creator_id = -1;
        this.title = "";
        if(border != null){
            this.border = new ArrayList<>(border);
        }else{
            this.border = new ArrayList<>();
        }
    }
    @Ignore
    public LandData(long creator_id, String title) {
        this.id = -1;
        this.creator_id = creator_id;
        this.title = title;
        this.border = new ArrayList<>();
    }
    @Ignore
    public LandData(long creator_id, String title,List<LatLng> border) {
        this.id = -1;
        this.creator_id = creator_id;
        this.title = title;
        if(border != null){
            this.border = new ArrayList<>(border);
        }else{
            this.border = new ArrayList<>();
        }
    }
    @Ignore
    public LandData(boolean setId, long creator_id, String title, List<LatLng> border) {
        if(setId){
            this.id = -1;
        }
        this.creator_id = creator_id;
        this.title = title;
        if(border != null){
            this.border = new ArrayList<>(border);
        }else{
            this.border = new ArrayList<>();
        }
    }
    public LandData(long id, long creator_id, String title, List<LatLng> border) {
        this.id = id;
        this.creator_id = creator_id;
        this.title = title;
        if(border != null){
            this.border = new ArrayList<>(border);
        }else{
            this.border = new ArrayList<>();
        }
    }

    public long getId() {
        return id;
    }
    public long getCreator_id() {
        return creator_id;
    }
    public String getTitle() {
        return title;
    }
    public List<LatLng> getBorder() {
        return border;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setCreator_id(long creator_id) {
        this.creator_id = creator_id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setBorder(List<LatLng> border){
        this.border.clear();
        this.border.addAll(border);
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
        dest.writeTypedList(border);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandData landData = (LandData) o;
        return
                id == landData.id &&
                creator_id == landData.creator_id &&
                title.equals(landData.title) &&
                ListUtils.arraysMatch(border,landData.border);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator_id, title, border);
    }
}
