package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPointRecord;
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
            userLands.add(new Land(
                    landData,
                    db.landPointDao().getAllLandPointsByLid(landData.getId())
            ));
        }

        return userLands;
    }

    @Override
    public Land getLand(long lid) {
        LandData landData = db.landDao().getLandData(lid);
        List<LandPoint> landPoints = db.landPointDao().getAllLandPointsByLid(lid);
        return new Land(landData,landPoints);
    }
    @Override
    public LandRecord getLandRecord(long id) {
        LandDataRecord landDataRecord = db.landHistoryDao().getLandRecord(id);
        List<LandPointRecord> landPointRecords =
                db.landPointHistoryDao().getLandPointHistoryByLRID(landDataRecord.getId());
        return new LandRecord(landDataRecord,landPointRecords);
    }
    @Override
    public List<LandRecord> getLandRecordsByLand(Land land) {
        List<LandRecord> landRecords = new ArrayList<>();
        if(land.getData() != null){
            List<LandDataRecord> landDataRecords = db.landHistoryDao().getLandRecordByLandID(land.getData().getId());
            for(LandDataRecord landDataRecord : landDataRecords){
                List<LandPointRecord> landPointRecords =
                        db.landPointHistoryDao().getLandPointHistoryByLRID(landDataRecord.getId());
                landRecords.add(new LandRecord(landDataRecord,landPointRecords));
            }
        }
        return landRecords;
    }
    @Override
    public List<LandRecord> getLandRecordsByUser(User user) {
        List<LandRecord> landRecords = new ArrayList<>();
        List<LandDataRecord> landDataRecords = db.landHistoryDao().getLandRecordsByUserId(user.getId());
        List<LandPointRecord> landPointRecords;
        for(LandDataRecord landDataRecord : landDataRecords){
            landPointRecords =
                    db.landPointHistoryDao().getLandPointHistoryByLRID(landDataRecord.getId());
            landRecords.add(new LandRecord(
                    landDataRecord,
                    landPointRecords
            ));
        }
        return landRecords;
    }

    @Override
    public Land saveLand(Land land){
        LandData landData = land.getData();
        List<LandPoint> landPoints = land.getBorder();
        if(landData != null){
            long id = db.landDao().insert(landData);
            landData.setId(id);
            land.setData(landData);
            if(landPoints.size()>0){
                db.landPointDao().deleteAllByLID(landData.getId());
                for(LandPoint landPoint:landPoints){
                    landPoint.setLid(landData.getId());
                    landPoint.setId(db.landPointDao().insert(landPoint));
                }
                land.setBorder(landPoints);
            }
        }
        return land;
    }
    @Override
    public void saveLandRecord(LandRecord landRecord) {
        LandDataRecord landRecordData = landRecord.getLandData();
        List<LandPointRecord> landRecordPoints = landRecord.getLandPoints();

        long LRid = db.landHistoryDao().insert(landRecordData);
        landRecordData.setId(LRid);
        landRecord.setLandData(landRecordData);

        if(landRecordPoints.size() > 0){
            long LPRid;
            for(LandPointRecord landRecordPoint : landRecordPoints){
                landRecordPoint.setLandRecordID(LRid);
                LPRid = db.landPointHistoryDao().insert(landRecordPoint);
                landRecordPoint.setId(LPRid);
            }
            landRecord.setLandPoints(landRecordPoints);
        }
    }

    @Override
    public void deleteLand(Land land) {
        LandData landData = land.getData();
        db.landPointDao().deleteAllByLID(landData.getId());
        db.landDao().delete(landData);
    }
    @Override
    public void deleteLandRecord(LandRecord landRecord) {
        LandDataRecord landRecordData = landRecord.getLandData();
        db.landPointHistoryDao().deleteAllByLRID(landRecordData.getId());
        db.landHistoryDao().delete(landRecordData);
    }
    @Override
    public void deleteLandsByUser(User user) {
        db.landDao().deleteByUID(user.getId());
    }

    @Override
    public boolean landExist(Land newLand) {
        LandData land = db.landDao().getLandData(newLand.getData().getId());
        return land != null;
    }

}
