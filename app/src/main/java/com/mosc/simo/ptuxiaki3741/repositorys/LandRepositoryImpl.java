package com.mosc.simo.ptuxiaki3741.repositorys;

import com.mosc.simo.ptuxiaki3741.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandData;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.ArrayList;
import java.util.List;

public class LandRepositoryImpl implements LandRepository {
    private final AppDatabase db;

    public LandRepositoryImpl(AppDatabase db){
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
            landData = saveLandData(landData);
            land.setLandData(landData);

            if(landPoints.size()>0){
                landPoints = initLandPoints(landData,landPoints);
                landPoints = saveLandPoints(landPoints);
                land.setLandPoints(landPoints);
            }
        }
        return land;
    }
    @Override
    public void deleteLand(Land land) {
        LandData landData = land.getLandData();
        db.landPointDao().deleteByLID(landData.getId());
        db.landDao().delete(landData);
    }

    private LandData saveLandData(LandData landData){
        long id = db.landDao().insert(landData);
        landData.setId(id);
        return landData;
    }
    private List<LandPoint> initLandPoints(LandData landData, List<LandPoint> landPoints) {
        db.landPointDao().deleteByLID(landData.getId());
        List<LandPoint> result = new ArrayList<>();
        for(LandPoint landPoint:landPoints){
            landPoint.setLid(landData.getId());
            result.add(landPoint);
        }
        return result;
    }
    private List<LandPoint> saveLandPoints(List<LandPoint> landPoints){
        long[] pIDs = db.landPointDao().insertAll(landPoints);
        if(landPoints.size() == pIDs.length){
            for(int i = 0; i<landPoints.size(); i++){
                landPoints.get(i).setId(pIDs[i]);
            }
        }
        return landPoints;
    }
}
