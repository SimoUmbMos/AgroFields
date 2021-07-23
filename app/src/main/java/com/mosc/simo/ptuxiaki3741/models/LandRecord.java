package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LandRecord {
    private LandDataRecord landData;
    private final List<LandPointRecord> landPoints;

    public LandRecord(Land land, User user, LandDBAction actionID, Date date){
        landPoints = new ArrayList<>();
        landData = new LandDataRecord(land.getData(),user,actionID,date);
        for(LandPoint landPoint : land.getBorder()){
            landPoints.add(new LandPointRecord(landData,landPoint));
        }
    }

    public LandRecord(LandData landData, List<LandPoint>  landPoints, User user, LandDBAction actionID, Date date){
        this.landPoints = new ArrayList<>();
        this.landData = new LandDataRecord(landData,user,actionID,date);
        for(LandPoint landPoint : landPoints){
            this.landPoints.add(new LandPointRecord(this.landData,landPoint));
        }
    }

    public LandRecord(LandDataRecord landData,List<LandPointRecord> landPoints){
        this.landData = landData;
        this.landPoints = new ArrayList<>(landPoints);
    }

    public LandDataRecord getLandData() {
        return landData;
    }
    public void setLandData(LandDataRecord landData) {
        this.landData = landData;
    }
    public List<LandPointRecord> getLandPoints() {
        return landPoints;
    }
    public void setLandPoints(List<LandPointRecord> landPoints) {
        this.landPoints.clear();
        this.landPoints.addAll(landPoints);
    }
}
