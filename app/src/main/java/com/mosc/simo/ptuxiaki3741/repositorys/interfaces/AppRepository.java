package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.List;
import java.util.Map;

public interface AppRepository {
    List<Land> getLands();
    Map<Long,List<LandZone>> getLandZones();
    List<LandDataRecord> getLandRecords();

    Land getLandByID(long id);
    List<LandZone> getLandZonesByLID(long lid);

    void saveLand(Land land);
    void saveZone(LandZone zone);
    void saveLandRecord(LandDataRecord landRecord);

    void deleteLand(Land land);
    void deleteZone(LandZone zone);
    void deleteZonesByLandID(long lid);

}
