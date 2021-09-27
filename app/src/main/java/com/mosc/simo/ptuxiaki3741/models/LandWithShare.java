package com.mosc.simo.ptuxiaki3741.models;

import androidx.room.Embedded;

import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.SharedLand;

public class LandWithShare {
    @Embedded
    private LandData data;
    @Embedded
    private SharedLand sharedData;

    public LandWithShare(LandData data, SharedLand sharedData){
        setData(data);
        setSharedData(sharedData);
    }

    public LandData getData() {
        return data;
    }
    public SharedLand getSharedData() {
        return sharedData;
    }

    public void setData(LandData data) {
        this.data = data;
    }
    public void setSharedData(SharedLand sharedData) {
        this.sharedData = sharedData;
    }
}
