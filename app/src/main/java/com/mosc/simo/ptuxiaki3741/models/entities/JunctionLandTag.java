package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "JunctionLandTag",
        primaryKeys = {
                "land_id",
                "tag_id"
        },
        indices = {
                @Index("land_id"),
                @Index("tag_id")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = LandData.class,
                        parentColumns = "id",
                        childColumns = "land_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = TagData.class,
                        parentColumns = "id",
                        childColumns = "tag_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class JunctionLandTag {
    @ColumnInfo(name="land_id")
    private final long lid;
    @ColumnInfo(name="tag_id")
    private final long tid;

    public JunctionLandTag(long lid, long tid) {
        this.lid = lid;
        this.tid = tid;
    }

    public long getLid() {
        return lid;
    }
    public long getTid() {
        return tid;
    }
}
