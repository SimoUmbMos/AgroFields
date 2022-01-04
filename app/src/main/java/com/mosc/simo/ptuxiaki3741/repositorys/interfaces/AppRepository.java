package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.TagData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppRepository {
    List<Land> getLands();
    Map<Long,List<LandZone>> getLandZones();
    List<LandDataRecord> getLandRecords();
    List<TagData> getTags();
    Map<LocalDate,List<CalendarNotification>> getNotifications();

    Land getLandByID(long id);
    List<LandZone> getLandZonesByLID(long lid);
    CalendarNotification getNotification(long id);

    void saveLand(Land land);
    void saveZone(LandZone zone);
    void saveLandRecord(LandDataRecord landRecord);
    void saveTag(TagData tag);
    void saveNotification(CalendarNotification notification);

    void deleteLand(Land land);
    void deleteZone(LandZone zone);
    void deleteTag(TagData tag);
    void deleteNotification(CalendarNotification notification);

}
