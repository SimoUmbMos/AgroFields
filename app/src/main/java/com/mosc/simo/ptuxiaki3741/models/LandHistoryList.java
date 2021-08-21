package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.util.LandUtil;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryList {
    private Land land;
    private final List<LandRecord> landRecords;

    public LandHistoryList(List<LandRecord> landRecords){
        this.landRecords = new ArrayList<>();
        if(landRecords.size()>0){
            land = LandUtil.getLandFromLandRecord(landRecords.get(landRecords.size()-1));
            this.landRecords.addAll(landRecords);
        }else{
            land = null;
        }
    }

    public Land getLand() {
        return land;
    }
    public List<LandRecord> getLandRecords() {
        return landRecords;
    }

    public void setLand(Land land) {
        this.land = land;
    }
    public void setLandRecords(List<LandRecord> landRecords) {
        this.landRecords.clear();
        this.landRecords.addAll(landRecords);
    }
}
