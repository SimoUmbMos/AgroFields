package com.mosc.simo.ptuxiaki3741.backend.file.openxml;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;

import java.util.ArrayList;
import java.util.List;

public class OpenXmlState {
    private final List<Land> lands;
    private final List<LandZone> zones;
    private final List<CalendarNotification> notifications;
    private final List<CalendarCategory> categories;

    public OpenXmlState() {
        lands = new ArrayList<>();
        zones = new ArrayList<>();
        notifications = new ArrayList<>();
        categories = new ArrayList<>();
    }
    public OpenXmlState(List<Land> lands, List<LandZone> zones, List<CalendarNotification> notifications, List<CalendarCategory> categories) {
        this.lands = new ArrayList<>(lands);
        this.zones = new ArrayList<>(zones);
        this.notifications = new ArrayList<>(notifications);
        this.categories = new ArrayList<>(categories);
    }

    public List<Land> getLands() {
        return lands;
    }
    public List<LandZone> getZones() {
        return zones;
    }
    public List<CalendarNotification> getNotifications() {
        return notifications;
    }
    public List<CalendarCategory> getCategories() {
        return categories;
    }

    public void clear(){
        lands.clear();
        zones.clear();
        notifications.clear();
        categories.clear();
    }
}
