package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandWithShare;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.List;

public interface LandRepository {

    List<Land> searchLandsByUser(User user);
    List<Land> getSharedLands(User user);

    Land getLand(long lid);
    List<LandDataRecord> getLandRecordsByLand(Land land);
    List<LandDataRecord> getLandRecordsByUser(User user);

    Land saveLand(Land land);
    void saveLandRecord(LandDataRecord landRecord);

    void deleteLand(Land land);

    void addSharedLand(User user, Land land);
    void removeSharedLand(User user, Land land);
    void removeAllSharedLands(User user1, User user2);
    List<LandWithShare> getSharedLandsToUser(User owner, User sharedUser);
    List<LandWithShare> getSharedLandsToOtherUsers(User user);
}
