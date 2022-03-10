package com.mosc.simo.ptuxiaki3741.backend.repository;

import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.entities.Snapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppRepository {

    void setDefaultSnapshot(Snapshot snapshot);
    Snapshot getDefaultSnapshot();
    Snapshot saveSnapshot(Snapshot snapshot);
    List<Snapshot> getSnapshots();

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
