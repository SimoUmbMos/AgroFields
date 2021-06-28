package com.mosc.simo.ptuxiaki3741.database.repositorys;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;

import java.util.ArrayList;
import java.util.List;

public class LandRepository {
    private final AppDatabase db;

    public LandRepository(Context context){
        db = MainActivity.getDb(context);
    }

    public Land saveLand(long user_id, String title){
        long id = db.landDao().insert(new Land(user_id,title));
        return new Land(id,user_id,title);
    }

    public Land updateLand(Land land) {
        long id = db.landDao().insert(land);
        return new Land(id,land.getCreator_id(),land.getTitle());
    }

    public List<LandPoint> getLandPoints(long land_id){
        return db.landPointDao().getAllLandPointsByLid(land_id);
    }

    public List<Land> getLands(long user_id){
        return db.landDao().getLandByCreatorId(user_id);
    }

    public void removeAllPointsByLID(long lid) {
        db.landPointDao().deleteByLID(lid);
    }

    public void addAllPoints(long lid, List<LatLng> points) {
        List<LandPoint> pointList = new ArrayList<>();
        for(LatLng point:points){
            pointList.add(new LandPoint(lid,points.indexOf(point),point));
        }
        db.landPointDao().insertAll(pointList);
    }

    public void deleteLand(Land land) {
        removeAllPointsByLID(land.getId());
        db.landDao().delete(land);
    }
}
