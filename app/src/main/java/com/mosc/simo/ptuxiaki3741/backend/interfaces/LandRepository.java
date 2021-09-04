package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public interface LandRepository {

    List<Land> searchLandsByUser(User user);

    Land getLand(long lid);
    List<LandDataRecord> getLandRecordsByLand(Land land);
    List<LandDataRecord> getLandRecordsByUser(User user);

    Land saveLand(Land land);
    void saveLandRecord(LandDataRecord landRecord);

    void deleteLand(Land land);
    void deleteLandsByUser(User user);
}
