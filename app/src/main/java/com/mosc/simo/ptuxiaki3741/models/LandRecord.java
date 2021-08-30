package com.mosc.simo.ptuxiaki3741.models;

import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.Date;
import java.util.Objects;

public class LandRecord {
    private LandDataRecord landData;

    public LandRecord(Land land, User user, LandDBAction actionID, Date date){
        landData = new LandDataRecord(land.getData(),user,actionID,date);
    }

    public LandRecord(LandDataRecord landData){
        this.landData = landData;
    }

    public LandDataRecord getLandData() {
        return landData;
    }

    public void setLandData(LandDataRecord landData) {
        this.landData = landData;
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
