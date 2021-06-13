package com.mosc.simo.ptuxiaki3741.database.helpers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.database.model.User;

public class LandHelper {
    private final AppDatabase db;

    public LandHelper(Context context){
        db = MainActivity.getDb(context);
    }

    public Land saveLand(long user_id, String title){
        long id = db.landDao().insert(new Land(user_id,title));
        db.close();
        return new Land(id,user_id,title);
    }

    public LandPoint saveLandPoint(Land land,int position , LatLng latLng){
        long id = db.landPointDao().insert(new LandPoint(land.getId(), position, latLng));
        db.close();
        return new LandPoint(id,land.getId(), position, latLng);
    }
}
