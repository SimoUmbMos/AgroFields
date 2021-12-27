package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "JunctionZoneTag",
        primaryKeys = {
                "zone_id",
                "tag_id"
        },
        indices = {
                @Index("zone_id"),
                @Index("tag_id")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = LandZoneData.class,
                        parentColumns = "id",
                        childColumns = "zone_id",
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
public class JunctionZoneTag {
    @ColumnInfo(name="zone_id")
    private final long zid;
    @ColumnInfo(name="tag_id")
    private final long tid;

    public JunctionZoneTag(long zid, long tid) {
        this.zid = zid;
        this.tid = tid;
    }

    public long getZid() {
        return zid;
    }
    public long getTid() {
        return tid;
    }
}
