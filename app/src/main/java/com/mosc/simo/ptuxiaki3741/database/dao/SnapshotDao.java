package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.Snapshot;

import java.util.List;

@Dao
public interface SnapshotDao {
    @Query("SELECT * FROM Snapshot WHERE `Key` = :key")
    Snapshot getSnapshot(long key);

    @Query("SELECT EXISTS( SELECT * FROM Snapshot WHERE `Key` = :key )")
    boolean snapshotExist(long key);

    @Query("Select * From Snapshot")
    List<Snapshot> getDataSnapshots();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Snapshot snapshot);

    @Delete
    void delete(Snapshot snapshot);
}
