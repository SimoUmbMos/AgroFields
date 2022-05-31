package com.mosc.simo.ptuxiaki3741.backend.repository;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppRepository {

    List<Land> getAllLands();
    List<Land> getLands(long year);
    List<Long> getLandYears();
    boolean landExist(long id);
    Land getLand(long id);
    void saveLand(Land land);
    void deleteLand(Land land);

    List<LandZone> getAllLandZones();
    Map<Long,List<LandZone>> getLandZones(long year);
    LandZone getLandZone(Long id);
    boolean zoneExist(long id);
    List<LandZone> getLandZonesByLandID(long id);
    void saveZone(LandZone zone);
    void deleteZone(LandZone zone);

    List<LandHistoryRecord> getLandRecords(long year);
    void saveLandRecord(LandHistoryRecord record);

    List<CalendarNotification> getAllNotifications();
    Map<LocalDate,List<CalendarEntity>> getNotifications();
    CalendarNotification getNotification(long id);
    List<CalendarNotification> getAllNotificationsByLid(long lid);
    List<CalendarNotification> getAllNotificationsByZid(long zid);
    void saveNotification(CalendarNotification notification);
    void deleteNotification(CalendarNotification notification);

    List<CalendarCategory> getCalendarCategories();
    CalendarCategory getCalendarCategory(long id);
    boolean calendarCategoryHasNotifications(long categoryID);
    void saveCalendarCategory(CalendarCategory category);
    void deleteCalendarCategory(CalendarCategory category);
}
