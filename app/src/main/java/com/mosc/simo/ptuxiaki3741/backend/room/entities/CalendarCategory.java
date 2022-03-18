package com.mosc.simo.ptuxiaki3741.backend.room.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mosc.simo.ptuxiaki3741.data.models.ColorData;

import java.util.Objects;

@Entity(tableName = "CalendarCategory")
public class CalendarCategory {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private long id;
    @ColumnInfo(name = "NAME")
    private String name;
    @ColumnInfo(name = "COLOR")
    private ColorData colorData;

    public CalendarCategory(long id, String name, ColorData colorData) {
        this.id = id;
        this.name = name;
        this.colorData = colorData;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ColorData getColorData() {
        return colorData;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColorData(ColorData colorData) {
        this.colorData = colorData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarCategory category = (CalendarCategory) o;
        return id == category.id && name.equals(category.name) && colorData.equals(category.colorData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, colorData);
    }
}
