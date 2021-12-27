package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "TagData")
public class TagData {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "Label")
    private String label;

    @Ignore
    public TagData(String label) {
        this.id = 0;
        this.label = label;
    }

    public TagData(long id, String label) {
        this.id = id;
        this.label = label;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagData tagData = (TagData) o;
        return label.equals(tagData.getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
