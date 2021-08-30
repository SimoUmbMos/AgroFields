package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public interface LandRepository {

    List<Land> searchLandsByUser(User user);

    Land getLand(long lid);
    List<LandRecord> getLandRecordsByLand(Land land);
    List<LandRecord> getLandRecordsByUser(User user);

    Land saveLand(Land land);
    void saveLandRecord(LandRecord landRecord);

    void deleteLand(Land land);
    void deleteLandsByUser(User user);
}
