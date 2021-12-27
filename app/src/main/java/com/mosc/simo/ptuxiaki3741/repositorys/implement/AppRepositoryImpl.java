package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRepositoryImpl implements AppRepository {
    private final RoomDatabase db;

    public AppRepositoryImpl(RoomDatabase db){
        this.db = db;
    }

    @Override
    public List<Land> getLands() {
        List<Land> lands = new ArrayList<>();

        List<LandData> landsData = db.landDao().getLands();
        if(landsData != null){
            for(LandData landData : landsData){
                lands.add(new Land(landData));
            }
        }
        return lands;
    }
    @Override
    public Map<Long,List<LandZone>> getLandZones(){
        Map<Long,List<LandZone>> ans = new HashMap<>();

        List<LandZone> zones;
        List<LandZoneData> zonesData = db.landZoneDao().getZones();
        if(zonesData != null){
            for(LandZoneData zoneData : zonesData){
                zones = ans.getOrDefault(zoneData.getLid(),null);
                if(zones == null){
                    zones = new ArrayList<>();
                }
                zones.add(new LandZone(zoneData));
                ans.put(zoneData.getLid(),zones);
            }
        }
        return ans;
    }
    @Override
    public List<LandDataRecord> getLandRecords() {
        return db.landHistoryDao().getLandRecords();
    }

    @Override
    public Land getLandByID(long id){
        LandData data = db.landDao().getLandByID(id);
        if(data != null)
            return new Land(data);
        return null;
    }
    @Override
    public List<LandZone> getLandZonesByLID(long lid){
        List<LandZone> ans = new ArrayList<>();

        List<LandZoneData> zones = db.landZoneDao().getLandZonesByLandID(lid);
        if(zones != null){
            for(LandZoneData zone : zones){
                ans.add(new LandZone(zone));
            }
        }
        return ans;
    }

    @Override
    public void saveLand(Land land){
        LandData landData = land.getData();
        if(landData != null){
            long id;
            id = db.landDao().insert(landData);
            landData.setId(id);
            land.setData(landData);
        }
    }
    @Override
    public void saveZone(LandZone zone) {
        LandZoneData zoneData = zone.getData();
        if(zoneData != null){
            long id = db.landZoneDao().insert(zoneData);
            zoneData.setId(id);
            zone.setData(zoneData);
        }
    }
    @Override
    public void saveLandRecord(LandDataRecord landRecord) {
        long LRid = db.landHistoryDao().insert(landRecord);
        landRecord.setId(LRid);
    }

    @Override
    public void deleteLand(Land land) {
        if(land.getData() != null)
            db.landDao().delete(land.getData());
    }
    @Override
    public void deleteZone(LandZone zone) {
        if(zone.getData() != null)
            db.landZoneDao().delete(zone.getData());
    }
}
