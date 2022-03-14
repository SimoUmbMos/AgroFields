package com.mosc.simo.ptuxiaki3741.data.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterItem;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;

import java.util.ArrayList;
import java.util.List;

public class ClusterLand implements ClusterItem {
    private final LandData landData;
    private final List<LandZoneData> zonesData;

    public ClusterLand(Land land, List<LandZone> zones) {
        zonesData = new ArrayList<>();
        if(land != null && land.getData() != null){
            landData = land.getData();
        }else{
            landData = new LandData(new ArrayList<>());
        }
        if(zones != null){
            for(LandZone zone : zones){
                if(zone != null && zone.getData() != null) zonesData.add(zone.getData());
            }
        }
    }

    public ClusterLand(LandData data) {
        if(data != null){
            landData = data;
        }else{
            landData = new LandData(new ArrayList<>());
        }
        zonesData = new ArrayList<>();
    }

    public LandData getLandData() {
        return landData;
    }

    public List<LandZoneData> getZonesData() {
        return zonesData;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        LatLng center = new LatLng(0,0);
        int size = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : landData.getBorder()){
            size++;
            builder.include(point);
        }
        if(size>0) center = builder.build().getCenter();
        return center;
    }

    @Nullable
    @Override
    public String getTitle() {
        return landData.toString();
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
