package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.LandWithShare;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.SharedLand;
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
            if(landData.getCreator_id() == -1){
                return land;
            }
            long id;
            if(landData.getId() != -1){
                id = db.landDao().insert(landData);
            }else{
                id = db.landDao().insert(new LandData(
                        false,
                        landData.getCreator_id(),
                        landData.getTitle(),
                        landData.getBorder(),
                        landData.getHoles()
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
        db.sharedLandDao().deleteByLandID(landData.getId());
        db.landDao().delete(landData);
    }

    @Override
    public List<Land> getSharedLands(User user) {
        List<Land> lands = new ArrayList<>();
        List<LandData> t = db.landDao().getUserSharedLands(user.getId());
        if(t != null)
            for(LandData t1:t){
                lands.add(new Land(t1));
            }
        return lands;
    }
    @Override
    public List<LandWithShare> getSharedLandsToOtherUsers(User user) {
        return db.sharedLandDao().getSharedLandsToOtherUsers(user.getId());
    }
    @Override
    public List<LandWithShare> getSharedLandsToUser(User owner, User sharedUser) {
        return db.sharedLandDao().getSharedLandsToUser(owner.getId(),sharedUser.getId());
    }
    @Override
    public void addSharedLand(User user, Land land) {
        if(user == null)
            return;
        if(land == null)
            return;
        if(land.getData() == null)
            return;

        List<SharedLand> sharedLands =
                db.sharedLandDao().getSharedLandsByUidAndLid(user.getId(),land.getData().getId());

        if(sharedLands != null){
            if(sharedLands.size()>0){
                return;
            }
        }

        SharedLand entry = new SharedLand(user.getId(), land.getData().getId());
        db.sharedLandDao().insert(entry);
    }
    @Override
    public void removeSharedLand(User user, Land land) {
        if(user == null)
            return;
        if(land == null)
            return;
        if(land.getData() == null)
            return;

        List<SharedLand> sharedLands =
                db.sharedLandDao().getSharedLandsByUidAndLid(user.getId(),land.getData().getId());

        if(sharedLands != null){
            if(sharedLands.size()>0){
                db.sharedLandDao().deleteAll(sharedLands);
            }
        }
    }
    @Override
    public void removeAllSharedLands(User user1, User user2) {
        if(user1 == null)
            return;
        if(user2 == null)
            return;

        List<SharedLand> sharedLands = new ArrayList<>();
        List<SharedLand> temp1 =
                db.sharedLandDao().getSharedLandsByCreatorIDAndUid(user1.getId(),user2.getId());
        List<SharedLand> temp2 =
                db.sharedLandDao().getSharedLandsByCreatorIDAndUid(user1.getId(),user2.getId());

        if(temp1 != null)
            sharedLands.addAll(temp1);
        if(temp2 != null)
            sharedLands.addAll(temp2);

        if(sharedLands.size() > 0)
            db.sharedLandDao().deleteAll(sharedLands);
    }
}
