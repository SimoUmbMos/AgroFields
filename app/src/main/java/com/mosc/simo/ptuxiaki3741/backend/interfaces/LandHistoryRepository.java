package com.mosc.simo.ptuxiaki3741.backend.interfaces;

import com.mosc.simo.ptuxiaki3741.models.LandRecord;

import java.util.List;

public interface LandHistoryRepository {
    LandRecord getLandRecord(long id);
    List<LandRecord> getLandRecordsByCreatorID(long uid);
    LandRecord saveLandRecord(LandRecord landRecord);
    void deleteLandRecord(LandRecord landRecord);
}
