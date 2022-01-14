package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mosc.simo.ptuxiaki3741.models.ColorData;

import java.util.Objects;

@Entity(tableName = "CalendarTagData")
public class CalendarTagData {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Label")
    private String label;
    @ColumnInfo(name = "Color")
    private ColorData color;

    public CalendarTagData(String label, ColorData color) {
        this.id = 0;
        this.label = label;
        this.color = color;
    }
    public CalendarTagData(long id, String label, ColorData color) {
        this.id = id;
        this.label = label;
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ColorData getColor() {
        return color;
    }

    public void setColor(ColorData color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarTagData tagData = (CalendarTagData) o;
        return label.equals(tagData.getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
