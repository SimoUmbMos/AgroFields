package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.util.LandUtil;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryList {
    private Land land;
    private boolean isVisible;
    private final List<LandRecord> landRecords;

    public LandHistoryList(List<LandRecord> landRecords){
        this.landRecords = new ArrayList<>();
        if(landRecords.size()>0){
            land = LandUtil.getLandFromLandRecord(landRecords.get(landRecords.size()-1));
            this.landRecords.addAll(landRecords);
        }else{
            land = null;
        }
        isVisible = false;
    }

    public Land getLand() {
        return land;
    }
    public boolean isVisible() {
        return isVisible;
    }
    public List<LandRecord> getLandRecords() {
        return landRecords;
    }

    public void setLand(Land land) {
        this.land = land;
    }
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
