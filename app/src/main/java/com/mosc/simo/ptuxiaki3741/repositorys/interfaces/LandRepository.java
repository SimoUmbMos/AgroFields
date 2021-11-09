package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;

import java.util.List;

public interface LandRepository {

    List<Land> searchLandsByUser(User user);

    Land getLand(long lid);
    List<LandDataRecord> getLandRecordsByLand(Land land);
    List<LandDataRecord> getLandRecordsByUser(User user);

    Land saveLand(Land land);
    void saveLandRecord(LandDataRecord landRecord);

    void deleteLand(Land land);

    boolean setLandNewCreator(long uid1, long uid2, long lid);
    UserLandPermissions getLandPermissionsForUser(User contact, Land land);
    void addLandPermissions(UserLandPermissions perms);
    void removeLandPermissions(User user, LandData landData);
    void removeAllLandPermissions(User user1, User user2);

}
