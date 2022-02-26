package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneDataRecord;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryRecord {
    private LandDataRecord landData;
    private final List<LandZoneDataRecord> landZonesData;

    public LandHistoryRecord(LandDataRecord landData) {
        this.landData = landData;
        landZonesData = new ArrayList<>();
    }
    public LandHistoryRecord(LandDataRecord landData, List<LandZoneDataRecord> landZonesData) {
        this.landData = landData;
        this.landZonesData = new ArrayList<>();
        if(landZonesData != null){
            this.landZonesData.addAll(landZonesData);
        }
    }

    public LandDataRecord getLandData() {
        return landData;
    }
    public List<LandZoneDataRecord> getLandZonesData() {
        return landZonesData;
    }

    public void setLandData(LandDataRecord landData) {
        this.landData = landData;
    }
    public void setLandZonesData(List<LandZoneDataRecord> landZonesData) {
        this.landZonesData.clear();
        if(landZonesData != null){
            this.landZonesData.addAll(landZonesData);
        }
    }

}
