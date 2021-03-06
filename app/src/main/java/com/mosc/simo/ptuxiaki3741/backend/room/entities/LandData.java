package com.mosc.simo.ptuxiaki3741.backend.room.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.ParcelableHole;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Store Land sensitive data.
 *
 * @author      Simo Mos
 * @version     %I%, %G%
 */
@Entity(tableName = "LandData")
public class LandData implements Parcelable {

    /**
     * Represents the Land ID
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private long id;

    /**
     * Represents the Snapshot Key
     */
    @ColumnInfo(name = "Year")
    private long year;

    /**
     * Represents the Land Title
     */
    @ColumnInfo(name = "Title")
    private String title;

    /**
     * Represents the Land Tags
     */
    @ColumnInfo(name = "Tags")
    private String tags;

    /**
     * Contains the Land Color
     */
    @ColumnInfo(name = "Color")
    private ColorData color;

    /**
     * Contains the Land Border
     */
    @ColumnInfo(name = "Border")
    private final List<LatLng> border;

    /**
     * Contains the Land Holes
     */
    @ColumnInfo(name = "Holes")
    private final List<List<LatLng>> holes;

    /**
     * Creates Land Data from another Land Data.
     * @param clone A LandData to clone
     */
    @Ignore
    public LandData(LandData clone) {
        id = clone.id;
        year = clone.year;
        title = clone.title;
        tags = clone.tags;
        color = new ColorData(clone.color.toString());
        border = new ArrayList<>();
        holes = new ArrayList<>();
        setBorder(clone.border);
        setHoles(clone.holes);
    }

    /**
     * Creates Land Data from a parcel.
     * @param parcel A Parcel containing the land data
     */
    @Ignore
    protected LandData(Parcel parcel) {
        id = parcel.readLong();
        year = parcel.readLong();
        title = parcel.readString();
        tags = parcel.readString();
        color = parcel.readParcelable(ColorData.class.getClassLoader());
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        List<LatLng> border = parcel.createTypedArrayList(LatLng.CREATOR);
        List<ParcelableHole> holes = parcel.createTypedArrayList(ParcelableHole.CREATOR);
        setBorder(border);
        this.holes.addAll(holes);
    }

    /**
     * Creates Land Data with the Land border
     * @param border A List&lt;LatLng&gt; containing the land border
     */
    @Ignore
    public LandData(List<LatLng> border) {
        this.id = 0;
        this.year = -1;
        this.title = "";
        this.tags = "";
        this.color = new ColorData(20, 249, 80);
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(border);
    }

    /**
     * Creates Land Data with the Land border and holes
     * @param border A List&lt;LatLng&gt; containing the land border
     * @param holes A List&lt;List&lt;LatLng&gt;&gt; containing the land holes
     */
    @Ignore
    public LandData(List<LatLng> border,List<List<LatLng>> holes) {
        this.id = 0;
        this.year = -1;
        this.title = "";
        this.tags = "";
        this.color = new ColorData(20, 249, 80);
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(border);
        setHoles(holes);
    }

    /**
     * Creates Land Data with the Land color and border
     * @param color A ColorData containing the land color
     * @param border A List&lt;LatLng&gt; containing the land border
     */
    @Ignore
    public LandData(ColorData color,List<LatLng> border) {
        this.id = 0;
        this.year = -1;
        this.title = "";
        this.color = color;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(border);
    }

    /**
     * Creates Land Data with the Land color, border and holes
     * @param color A ColorData containing the land color
     * @param border A List&lt;LatLng&gt; containing the land border
     * @param holes A List&lt;List&lt;LatLng&gt;&gt; containing the land holes
     */
    @Ignore
    public LandData(ColorData color, List<LatLng> border,List<List<LatLng>> holes) {
        this.id = 0;
        this.year = -1;
        this.title = "";
        this.tags = "";
        this.color = color;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(border);
        setHoles(holes);
    }

    /**
     * Creates Land Data with the Land snapshot key, title and color
     * @param year A long representing the land year
     * @param title  A String representing the land title
     * @param tags  A String representing the land tags
     * @param color A ColorData containing the land color
     */
    @Ignore
    public LandData(long year, String title, String tags, ColorData color) {
        this.id = 0;
        this.year = year;
        this.title = title;
        this.tags = tags;
        this.color = color;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
    }

    /**
     * Creates an Land Data with the specified values
     * @param id A long representing the land id
     * @param year A long representing the land year
     * @param title  A String representing the land title
     * @param tags  A String representing the land tags
     * @param color A ColorData containing the land color
     * @param border A List&lt;LatLng&gt; containing the land border
     * @param holes A List&lt;List&lt;LatLng&gt;&gt; containing the land holes
     */
    public LandData(long id, long year, String title, String tags, ColorData color, List<LatLng> border, List<List<LatLng>> holes) {
        this.id = id;
        this.year = year;
        this.title = title;
        this.tags = tags;
        this.color = color;
        this.border = new ArrayList<>();
        this.holes = new ArrayList<>();
        setBorder(border);
        setHoles(holes);
    }

    /**
     * Gets the Land id
     * @return A long representing the land id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the Land snapshot key
     * @return A long representing the land snapshot key
     */
    public long getYear() {
        return year;
    }

    /**
     * Gets the Land title
     * @return A String representing the land title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the Land tags
     * @return A String representing the land tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * Gets the Land color
     * @return A ColorData containing the land color
     */
    public ColorData getColor() {
        return color;
    }

    /**
     * Gets the Land border
     * @return A List&lt;LatLng&gt; containing the land border
     */
    public List<LatLng> getBorder() {
        return border;
    }

    /**
     * Gets the Land holes
     * @return A List&lt;List&lt;LatLng&gt;&gt; containing the land holes
     */
    public List<List<LatLng>> getHoles() {
        return holes;
    }

    /**
     * Sets the Land id
     * @param id A long representing the land id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Sets the Land snapshot key
     * @param year A long representing the land year
     */
    public void setYear(long year) {
        this.year = year;
    }

    /**
     * Sets the Land title
     * @param title A string representing the land title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the Land tags
     * @param tags A string representing the land tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Sets the Land color
     * @param color A ColorData containing the land color
     */
    public void setColor(ColorData color){
        this.color = color;
    }

    /**
     * Sets the Land border
     * @param border A List&lt;LatLng&gt; containing the land border
     */
    public void setBorder(List<LatLng> border){
        this.border.clear();
        if(border != null)
            this.border.addAll(border);

    }

    /**
     * Sets the Land holes
     * @param holes A List&lt;List&lt;LatLng&gt;&gt; containing the land holes
     */
    public void setHoles(List<List<LatLng>> holes){
        this.holes.clear();
        if(holes != null)
            this.holes.addAll(holes);
    }

    /**
     * Generates instance of LandData class from a Parcel
     */
    @Ignore
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

    /**
     * Describe LandData class content to parcel
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write LandData class content to parcel
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(year);
        dest.writeString(title);
        dest.writeString(tags);
        dest.writeParcelable(color,flags);
        dest.writeTypedList(border);
        List<ParcelableHole> holes = new ArrayList<>();
        for(List<LatLng> hole: this.holes)
            holes.add(new ParcelableHole(hole));
        dest.writeTypedList(holes);
    }

    /**
     * Compare two LandData classes
     * @return A boolean
     * <ul>
     * <li> true - if same object or equal
     * <li> false - if other object is null or not equal
     * </ul>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandData landData = (LandData) o;
        return
                id == landData.id &&
                year == landData.year &&
                title.equals(landData.title) &&
                tags.equals(landData.tags) &&
                color.equals(landData.color) &&
                ListUtils.arraysMatch(border,landData.border) &&
                ListUtils.arraysMatch(holes,landData.holes);
    }

    /**
     * Gets LandData content hash code
     * @return A int representing the land content hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, year, title, tags, color, border, holes);
    }

    /**
     * Convert LandData content to String
     * @return A String containing the id and title of the land
     */
    @NonNull
    @Override
    public String toString() {
        if(id > 0) return "#" + id + " " + title;
        return title;
    }
}
