package com.mosc.simo.ptuxiaki3741.backend.repositorys;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandData;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.User;

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
    public Land saveLand(Land land){
        LandData landData = land.getLandData();
        List<LandPoint> landPoints = land.getLandPoints();
        if(landData != null){
            saveLandData(landData);
            land.setLandData(landData);
            if(landPoints.size()>0){
                saveLandPoints(landData,landPoints);
                land.setLandPoints(landPoints);
            }
        }
        return land;
    }
    @Override
    public void deleteLand(Land land) {
        LandData landData = land.getLandData();
        db.landPointDao().deleteAllByLID(landData.getId());
        db.landDao().delete(landData);
    }

    private void saveLandData(LandData landData){
        long id = db.landDao().insert(landData);
        landData.setId(id);
    }
    private void saveLandPoints(LandData landData,List<LandPoint> landPoints){
        long pID;
        db.landPointDao().deleteAllByLID(landData.getId());
        for(LandPoint landPoint:landPoints){
            landPoint.setLid(landData.getId());
            pID = db.landPointDao().insert(landPoint);
            landPoint.setId(pID);
        }
    }
}
