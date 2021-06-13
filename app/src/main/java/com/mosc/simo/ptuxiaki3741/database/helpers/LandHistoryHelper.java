package com.mosc.simo.ptuxiaki3741.database.helpers;

import android.content.Context;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.database.model.LandPointRecord;
import com.mosc.simo.ptuxiaki3741.database.model.LandRecord;
import com.mosc.simo.ptuxiaki3741.database.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LandHistoryHelper {

    private final AppDatabase db;

    public LandHistoryHelper(Context context){
        db = MainActivity.getDb(context);
    }

    public void putLandToLandHistory(Land land, User user, int actionID, Date date,
                                            List<LandPoint> landPoints){
        LandRecord landRecord = new LandRecord(land,user,actionID,date);
        LandPointRecord temp;
        for (LandPoint landPointRecord: landPoints) {
            temp = new LandPointRecord(landRecord,landPointRecord);
        }
        //todo add landRecord with LandPoints to db
    }

    public List<LandRecord> getLandHistory(Land land){
        //todo get all landRecord from land
        return new ArrayList<>();
    }

    public void restoreLandFromLandHistory(Land land,LandRecord landRecord){
        //todo delete all landPoint from land and replace with landPointRecord of landRecord
    }
}
