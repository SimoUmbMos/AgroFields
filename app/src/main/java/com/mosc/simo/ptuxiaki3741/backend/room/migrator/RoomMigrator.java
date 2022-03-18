package com.mosc.simo.ptuxiaki3741.backend.room.migrator;

import androidx.room.DeleteColumn;
import androidx.room.RenameColumn;
import androidx.room.migration.AutoMigrationSpec;

public class RoomMigrator {
    @DeleteColumn(tableName = "Snapshot", columnName = "CalendarCount")
    @RenameColumn(tableName = "CalendarNotification", fromColumnName = "Type", toColumnName = "CategoryID")
    public static class toVersion3 implements AutoMigrationSpec {}
}
