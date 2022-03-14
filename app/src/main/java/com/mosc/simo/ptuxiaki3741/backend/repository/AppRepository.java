package com.mosc.simo.ptuxiaki3741.backend.repository;

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
    Snapshot saveSnapshot(long snapshot);
    List<Long> getSnapshots();

    List<String> getLandTags();
    List<Land> getLands();
    Map<Long,List<LandZone>> getLandZones();
    List<LandHistoryRecord> getLandRecords();
    Map<LocalDate,List<CalendarNotification>> getNotifications();

    boolean landExist(long id, long snapshot);
    boolean zoneExist(long id, long snapshot);
    List<LandZone> getLandZonesByLandData(LandData data);
    CalendarNotification getNotification(long id, long snapshot);

    Land getLand(long id, long snapshot);
    void saveLand(Land land);
    void saveZone(LandZone zone);
    void saveLandRecord(LandHistoryRecord record);
    void saveNotification(CalendarNotification notification);

    void deleteLand(Land land);
    void deleteZone(LandZone zone);
    void deleteNotification(CalendarNotification notification);
}
