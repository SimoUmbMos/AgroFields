package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

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
    public List<Contact> getContacts() {
        return db.contactDao().getAllUsers();
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
    public List<Contact> userSearch(String search, int page) {
        if(page<1)
            page = 1;
        if(search.length() > 3){
            List<Contact> result = db.contactDao().searchUserByUserName(
                    search,
                    AppValues.DATABASE_PAGE_SIZE,
                    (page-1)*AppValues.DATABASE_PAGE_SIZE
            );
            if(result != null)
                return result;
        }
        return new ArrayList<>();
    }
    @Override
    public int searchUserMaxPage(String search) {
        if(search.length() > 3){
            List<Long> result = db.contactDao().searchUserByUserNamePage(search);
            if(result != null)
                return (result.size() - 1) / AppValues.DATABASE_PAGE_SIZE + 1;
        }
        return 1;
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
    public Contact saveContact(Contact contact){
        if(contact != null){
            long id = db.contactDao().insert(contact);
            contact.setId(id);
            return contact;
        }
        return null;
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
    @Override
    public void deleteContact(Contact contact){
        if(contact != null){
            db.contactDao().delete(contact);
        }
    }
}
