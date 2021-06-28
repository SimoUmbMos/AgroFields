package com.mosc.simo.ptuxiaki3741.database.repositorys;

import android.content.Context;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.database.enums.DBAction;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.database.model.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.database.model.LandRecord;
import com.mosc.simo.ptuxiaki3741.database.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LandHistoryRepository {

    private final AppDatabase db;

    public LandHistoryRepository(Context context){
        db = MainActivity.getDb(context);
    }

    public void putLandToLandHistory(Land land, User user, DBAction actionID, Date date){
        List<LandPoint> landPoints = db.landPointDao().getAllLandPointsByLid(land.getId());

        LandRecord landRecord = new LandRecord(land,user,actionID,date);
        List<LandPointRecord> landPointRecords = new ArrayList<>();
        for(LandPoint landPoint : landPoints){
            landPointRecords.add(new LandPointRecord(landRecord,landPoint));
        }

        db.landHistoryDao().insert(landRecord);
        db.landPointHistoryDao().insertAll(landPointRecords);
    }

    public List<LandRecord> getLandHistory(Land land){
        //todo get all landRecord from land
        return new ArrayList<>();
    }

    public void restoreLandFromLandHistory(LandRecord landRecord){
        //todo delete all landPoint from land and replace with landPointRecord of landRecord
    }
}
