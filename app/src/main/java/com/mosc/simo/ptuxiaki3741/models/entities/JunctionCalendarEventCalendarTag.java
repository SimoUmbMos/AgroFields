package com.mosc.simo.ptuxiaki3741.models.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "JunctionLandTag",
        primaryKeys = {
                "event_id",
                "tag_id"
        },
        indices = {
                @Index("event_id"),
                @Index("tag_id")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = CalendarNotification.class,
                        parentColumns = "id",
                        childColumns = "event_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CalendarTagData.class,
                        parentColumns = "id",
                        childColumns = "tag_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class JunctionCalendarEventCalendarTag {
    @ColumnInfo(name="event_id")
    private final long lid;
    @ColumnInfo(name="tag_id")
    private final long tid;

    public JunctionCalendarEventCalendarTag(long lid, long tid) {
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
