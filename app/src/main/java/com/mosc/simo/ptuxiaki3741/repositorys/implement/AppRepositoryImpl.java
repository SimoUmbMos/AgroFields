package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.List;

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
    public List<LandZone> getLandZones(){
        List<LandZone> ans = new ArrayList<>();

        List<LandZoneData> zones = db.landZoneDao().getZones();
        if(zones != null){
            for(LandZoneData zone : zones){
                ans.add(new LandZone(zone));
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
            if(landData.getId() != -1){
                id = db.landDao().insert(landData);
            }else{
                id = db.landDao().insert(new LandData(
                        false,
                        landData.getTitle(),
                        landData.getColor(),
                        landData.getBorder(),
                        landData.getHoles()
                ));
            }
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
        LandData landData = land.getData();
        db.landDao().delete(landData);
    }
    @Override
    public void deleteZone(LandZone zone) {
        LandZoneData zoneData = zone.getData();
        db.landZoneDao().delete(zoneData);
    }
    @Override
    public void deleteZonesByLandID(long lid) {
        db.landZoneDao().deleteByLID(lid);
    }
}
