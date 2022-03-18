package com.mosc.simo.ptuxiaki3741.backend.repository;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.Snapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppRepository {
    void setDefaultSnapshot(long snapshot);
    long getDefaultSnapshot();

    List<Long> getSnapshots();
    Snapshot saveSnapshot(long snapshot);

    List<Land> getLands();
    boolean landExist(long id, long snapshot);
    Land getLand(long id, long snapshot);
    void saveLand(Land land);
    void deleteLand(Land land);

    List<String> getLandTags();

    Map<Long,List<LandZone>> getLandZones();
    boolean zoneExist(long id, long snapshot);
    List<LandZone> getLandZonesByLandData(LandData data);
    void saveZone(LandZone zone);
    void deleteZone(LandZone zone);

    List<LandHistoryRecord> getLandRecords();
    void saveLandRecord(LandHistoryRecord record);

    Map<LocalDate,List<CalendarNotification>> getNotifications();
    CalendarNotification getNotification(long id);
    void saveNotification(CalendarNotification notification);
    void deleteNotification(CalendarNotification notification);

    List<CalendarCategory> getCalendarCategories();
    void saveCalendarCategory(CalendarCategory category);
    void deleteCalendarCategory(CalendarCategory category);
}
