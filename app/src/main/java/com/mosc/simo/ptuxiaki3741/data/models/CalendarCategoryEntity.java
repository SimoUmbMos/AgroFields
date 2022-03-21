package com.mosc.simo.ptuxiaki3741.data.models;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;

public class CalendarCategoryEntity {
    private final CalendarCategory data;
    private boolean isSelected;

    public CalendarCategoryEntity(CalendarCategory data) {
        this.data = data;
        this.isSelected = false;
    }

    public CalendarCategory getData() {
        return data;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
