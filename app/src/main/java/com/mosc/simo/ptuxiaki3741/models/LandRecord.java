package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LandRecord {
    private LandDataRecord landData;
    private final List<LandPointRecord> landPoints;

    public LandRecord(){
        landData = null;
        landPoints = new ArrayList<>();
    }

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
    public List<LandPointRecord> getLandPoints() {
        return landPoints;
    }

    public void setLandData(LandDataRecord landData) {
        this.landData = landData;
    }
    public void setLandPoints(List<LandPointRecord> landPoints) {
        this.landPoints.clear();
        this.landPoints.addAll(landPoints);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.getLandData().getId());
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else if (!(o instanceof LandRecord)) {
            return false;
        } else {
            if(this.getLandData() == null && ((LandRecord) o).getLandData() == null){
                return true;
            }else if(this.getLandData() != null && ((LandRecord) o).getLandData() != null){
                return ((LandRecord) o).getLandData().getId() == this.getLandData().getId();
            }else{
                return false;
            }
        }
    }
}
