package com.mosc.simo.ptuxiaki3741.models.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.models.ParcelableHole;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "LandData")
public class LandData implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Title")
    private String title;
    @ColumnInfo(name = "Color")
    private ColorData color;
    @ColumnInfo(name = "Border")
    private final List<LatLng> border;
    @ColumnInfo(name = "Holes")
    private final List<List<LatLng>> holes;

    @Ignore
    protected LandData(Parcel in) {
        id = in.readLong();
        title = in.readString();
        color = in.readParcelable(ColorData.class.getClassLoader());
        border = in.createTypedArrayList(LatLng.CREATOR);
        List<ParcelableHole> holes = in.createTypedArrayList(ParcelableHole.CREATOR);
        this.holes = new ArrayList<>();
        this.holes.addAll(holes);
    }
    @Ignore
    public LandData(List<LatLng> border) {
        this.id = -1;
        this.title = "";
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.color = AppValues.defaultLandColor;
        setBorder(border);
    }
    @Ignore
    public LandData(List<LatLng> border,List<List<LatLng>> holes) {
        this.id = -1;
        this.title = "";
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.color = AppValues.defaultLandColor;
        setBorder(border);
        setHoles(holes);
    }
    @Ignore
    public LandData(ColorData color, List<LatLng> border,List<List<LatLng>> holes) {
        this.id = -1;
        this.title = "";
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.color = color;
        setBorder(border);
        setHoles(holes);
    }
    @Ignore
    public LandData(String title, ColorData color) {
        this.id = -1;
        this.title = title;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.color = color;
    }
    @Ignore
    public LandData(boolean setId, String title, ColorData color, List<LatLng> border, List<List<LatLng>> holes) {
        if(setId){
            this.id = -1;
        }
        this.title = title;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.color = color;
        setBorder(border);
        setHoles(holes);
    }
    public LandData(long id, String title, ColorData color, List<LatLng> border, List<List<LatLng>> holes) {
        this.id = id;
        this.title = title;
        this.border = new ArrayList<>(border);
        this.holes = new ArrayList<>(holes);
        this.color = color;
    }

    public long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public ColorData getColor() {
        return color;
    }
    public List<LatLng> getBorder() {
        return border;
    }
    public List<List<LatLng>> getHoles() {
        return holes;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setBorder(List<LatLng> border){
        this.border.clear();
        if(border != null)
            this.border.addAll(border);

    }
    public void setHoles(List<List<LatLng>> holes){
        this.holes.clear();
        if(holes != null)
            this.holes.addAll(holes);
    }
    public void setColor(ColorData color){
        this.color = color;
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
        dest.writeString(title);
        dest.writeParcelable(color,flags);
        dest.writeTypedList(border);
        List<ParcelableHole> holes = new ArrayList<>();
        for(List<LatLng> hole: this.holes)
            holes.add(new ParcelableHole(hole));
        dest.writeTypedList(holes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandData landData = (LandData) o;
        return
                id == landData.id &&
                title.equals(landData.title) &&
                color.equals(landData.color) &&
                ListUtils.arraysMatch(border,landData.border) &&
                ListUtils.arraysMatch(holes,landData.holes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, color, border, holes);
    }
}
