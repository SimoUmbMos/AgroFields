package com.mosc.simo.ptuxiaki3741.backend.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;

import java.util.List;

@Dao
public interface CalendarCategoriesDao {
    @Query("SELECT * FROM CalendarCategory WHERE ID = :id LIMIT 1")
    CalendarCategory getCalendarCategory(long id);

    @Query("SELECT * FROM CalendarCategory")
    List<CalendarCategory> getCalendarCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CalendarCategory category);

    @Delete
    void delete(CalendarCategory category);
}
