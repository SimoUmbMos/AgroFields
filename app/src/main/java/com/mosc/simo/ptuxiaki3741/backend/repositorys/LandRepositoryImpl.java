package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
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
    public List<LandDataRecord> getLandRecordsByLand(Land land) {
        if(land.getData() != null){
            return db.landHistoryDao().getLandRecordByLandID(land.getData().getId());
        }
        return new ArrayList<>();
    }
    @Override
    public List<LandDataRecord> getLandRecordsByUser(User user) {
        return db.landHistoryDao().getLandRecordsByUserId(user.getId());
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
    public void deleteLandsByUser(User user) {
        db.landDao().deleteByUID(user.getId());
    }


}
