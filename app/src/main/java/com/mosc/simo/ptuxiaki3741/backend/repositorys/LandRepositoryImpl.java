package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.List;

public class LandRepositoryImpl implements LandRepository {
    private final RoomDatabase db;

    public LandRepositoryImpl(RoomDatabase db){
        this.db = db;
    }

    @Override
    public List<Land> searchLandsByUser(User user) {
        List<Land> userLands = new ArrayList<>();
        long uid = user.getId();

        List<LandData> userLandData = db.landDao().getLandByCreatorId(uid);
        for(LandData landData : userLandData){
            userLands.add(new Land(landData));
        }

        return userLands;
    }

    @Override
    public Land getLand(long lid) {
        LandData landData = db.landDao().getLandData(lid);
        return new Land(landData);
    }
    @Override
    public List<LandRecord> getLandRecordsByLand(Land land) {
        List<LandRecord> landRecords = new ArrayList<>();
        if(land.getData() != null){
            List<LandDataRecord> landDataRecords = db.landHistoryDao().getLandRecordByLandID(land.getData().getId());
            for(LandDataRecord landDataRecord : landDataRecords){
                landRecords.add(new LandRecord(landDataRecord));
            }
        }
        return landRecords;
    }
    @Override
    public List<LandRecord> getLandRecordsByUser(User user) {
        List<LandRecord> landRecords = new ArrayList<>();
        List<LandDataRecord> landDataRecords = db.landHistoryDao().getLandRecordsByUserId(user.getId());
        for(LandDataRecord landDataRecord : landDataRecords){
            landRecords.add(new LandRecord(landDataRecord));
        }
        return landRecords;
    }

    @Override
    public Land saveLand(Land land){
        LandData landData = land.getData();
        if(landData != null){
            long id;
            if(landData.getId() != -1){
                id = db.landDao().insert(landData);
            }else{
                id = db.landDao().insert(new LandData(
                        false,
                        landData.getCreator_id(),
                        landData.getTitle(),
                        landData.getBorder()
                ));
            }
            landData.setId(id);
            land.setData(landData);
        }
        return land;
    }
    @Override
    public void saveLandRecord(LandRecord landRecord) {
        LandDataRecord landRecordData = landRecord.getLandData();
        long LRid = db.landHistoryDao().insert(landRecordData);
        landRecordData.setId(LRid);
        landRecord.setLandData(landRecordData);
    }

    @Override
    public void deleteLand(Land land) {
        LandData landData = land.getData();
        db.landDao().delete(landData);
    }
    @Override
    public void deleteLandsByUser(User user) {
        db.landDao().deleteByUID(user.getId());
    }


}
