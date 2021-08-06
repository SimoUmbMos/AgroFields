package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public interface LandHistoryRepository {
    LandRecord getLandRecord(long id);
    List<LandRecord> getLandRecordsByUser(User user);
    LandRecord saveLandRecord(LandRecord landRecord);
    void deleteLandRecord(LandRecord landRecord);
}
