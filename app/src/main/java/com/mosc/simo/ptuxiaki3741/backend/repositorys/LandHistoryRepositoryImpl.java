package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import android.util.Log;

import com.mosc.simo.ptuxiaki3741.backend.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.LandHistoryRepository;
import com.mosc.simo.ptuxiaki3741.models.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;

import java.util.ArrayList;
import java.util.List;

public class LandHistoryRepositoryImpl implements LandHistoryRepository {
    private final AppDatabase db;
    public LandHistoryRepositoryImpl(AppDatabase db){
        this.db = db;
    }

    @Override
    public LandRecord getLandRecord(long id) {
        LandDataRecord landDataRecord = db.landHistoryDao().getLandRecord(id);
        List<LandPointRecord> landPointRecords =
                db.landPointHistoryDao().getLandPointHistoryByLRID(landDataRecord.getId());
        return new LandRecord(landDataRecord,landPointRecords);
    }

    @Override
    public List<LandRecord> getLandRecordsByCreatorID(long uid) {
        List<LandRecord> landRecords = new ArrayList<>();
        List<LandDataRecord> landDataRecords = db.landHistoryDao().getLandRecordsByUserId(uid);
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
    public LandRecord saveLandRecord(LandRecord landRecord) {
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
        return landRecord;
    }

    @Override
    public void deleteLandRecord(LandRecord landRecord) {
        LandDataRecord landRecordData = landRecord.getLandData();
        db.landPointHistoryDao().deleteAllByLRID(landRecordData.getId());
        db.landHistoryDao().delete(landRecordData);
    }
}
