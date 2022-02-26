package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LandHistory {
    private LandData landData;
    private boolean isVisible;
    private final List<LandHistoryRecord> landRecords;

    public LandHistory(List<LandHistoryRecord> records){
        if(records != null)
            landRecords = new ArrayList<>(records);
        else
            landRecords = new ArrayList<>();

        if(landRecords.size()>0){
            landData = LandUtil.getLandDataFromLandRecord(landRecords.get(landRecords.size()-1));
        }else{
            landData = null;
        }
        isVisible = false;
    }

    public LandData getLandData() {
        return landData;
    }
    public boolean isVisible() {
        return isVisible;
    }
    public List<LandHistoryRecord> getData() {
        return landRecords;
    }

    public void setLand(LandData land) {
        this.landData = land;
    }
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
    public void setData(List<LandHistoryRecord> landRecords) {
        this.landRecords.clear();
        this.landRecords.addAll(landRecords);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof LandHistory))
            return false;
        LandHistory that = (LandHistory) o;
        return ListUtils.arraysMatch(landRecords,that.landRecords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(landRecords);
    }

    public String getTitle() {
        return landData.getTitle()+ " #"+ landData.getId();
    }
}
