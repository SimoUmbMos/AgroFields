package com.mosc.simo.ptuxiaki3741.data.models;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;

import java.util.Objects;

public class CalendarEntity {
    private CalendarCategory category;
    private CalendarNotification notification;

    public CalendarEntity(CalendarCategory category, CalendarNotification notification) {
        this.category = category;
        this.notification = notification;
    }

    public CalendarCategory getCategory() {
        return category;
    }

    public void setCategory(CalendarCategory category) {
        this.category = category;
    }

    public CalendarNotification getNotification() {
        return notification;
    }

    public void setNotification(CalendarNotification notification) {
        this.notification = notification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarEntity entity = (CalendarEntity) o;
        return category.equals(entity.category) && notification.equals(entity.notification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, notification);
    }
}
