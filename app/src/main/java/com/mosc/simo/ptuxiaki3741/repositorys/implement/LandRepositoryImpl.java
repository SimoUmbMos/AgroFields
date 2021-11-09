package com.mosc.simo.ptuxiaki3741.repositorys.implement;

import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.List;

public class LandRepositoryImpl implements LandRepository {
    private final RoomDatabase db;

    public LandRepositoryImpl(RoomDatabase db){
        this.db = db;
    }

    @Override
    public List<Land> getLandsByUser(User user) {
        List<Land> userLands = new ArrayList<>();
        long uid = user.getId();

        List<LandData> userLandData = db.landDao().getLandByCreatorId(uid);
        for(LandData landData : userLandData){
            userLands.add(new Land(landData));
        }
        List<Land> lands = db.landDao().getUserSharedLands(uid);
        if(lands != null){
            userLands.addAll(lands);
        }
        return userLands;
    }

    @Override
    public Land getLand(long lid) {
        LandData landData = db.landDao().getLandData(lid);
        return new Land(landData);
    }
    @Override
    public List<LandDataRecord> getLandRecordsByUser(User user) {
        return db.landHistoryDao().getLandRecordsByUserIdAndPermission(user.getId(),true);
    }

    @Override
    public Land saveLand(Land land){
        LandData landData = land.getData();
        if(landData != null){
            if(landData.getCreator_id() == -1){
                return land;
            }
            long id;
            if(landData.getId() != -1){
                id = db.landDao().insert(landData);
            }else{
                id = db.landDao().insert(new LandData(
                        false,
                        landData.getCreator_id(),
                        landData.getTitle(),
                        landData.getBorder(),
                        landData.getHoles()
                ));
            }
            landData.setId(id);
            land.setData(landData);
        }
        return land;
    }
    @Override
    public void saveLandRecord(LandDataRecord landRecord) {
        long LRid = db.landHistoryDao().insert(landRecord);
        landRecord.setId(LRid);
    }
    @Override
    public void deleteLand(Land land) {
        LandData landData = land.getData();
        db.sharedLandDao().deleteByLandID(landData.getId());
        db.landDao().delete(landData);
    }

    @Override
    public boolean setLandNewCreator(long uid1, long uid2, long lid) {
        LandData data = db.landDao().getLandData(lid);
        User user1 = db.userDao().getUserById(uid1);
        User user2 = db.userDao().getUserById(uid2);
        if(data != null){
            if(user1 != null){
                data.setCreator_id(user1.getId());
                removeLandPermissions(user1,data);
                db.landDao().insert(data);
                if(user2 != null){
                    addLandPermissions(new UserLandPermissions(
                            user2.getId(),
                            data.getId(),
                            true,
                            true,
                            true
                    ));
                }
                return true;
            }
        }
        return false;
    }
    @Override
    public UserLandPermissions getLandPermissionsForUser(User contact, Land land) {
        List<UserLandPermissions> perms =
                db.sharedLandDao().getSharedLandsByUidAndLid(
                        contact.getId(),
                        land.getData().getId()
                );
        if(perms != null){
            if(perms.size()>0){
                return perms.get(0);
            }
        }
        return null;
    }
    @Override
    public void addLandPermissions(UserLandPermissions perms) {
        if(perms == null)
            return;

        List<UserLandPermissions> userLandPermissions =
                db.sharedLandDao().getSharedLandsByUidAndLid(perms.getUserID(), perms.getLandID());

        if(userLandPermissions != null){
            if(userLandPermissions.size()>0){
                db.sharedLandDao().deleteAll(userLandPermissions);
            }
        }
        if(perms.hasPerms()){
            db.sharedLandDao().insert(perms);
        }
    }
    @Override
    public void removeLandPermissions(User user, LandData landData) {
        if(user == null)
            return;
        if(landData == null)
            return;

        List<UserLandPermissions> userLandPermissions =
                db.sharedLandDao().getSharedLandsByUidAndLid(user.getId(),landData.getId());

        if(userLandPermissions != null){
            if(userLandPermissions.size()>0){
                db.sharedLandDao().deleteAll(userLandPermissions);
            }
        }
    }
    @Override
    public void removeAllLandPermissions(User user1, User user2) {
        if(user1 == null)
            return;
        if(user2 == null)
            return;

        List<UserLandPermissions> userLandPermissions = new ArrayList<>();
        List<UserLandPermissions> temp1 =
                db.sharedLandDao().getSharedLandsByCreatorIDAndUid(user1.getId(),user2.getId());
        List<UserLandPermissions> temp2 =
                db.sharedLandDao().getSharedLandsByCreatorIDAndUid(user2.getId(),user1.getId());

        if(temp1 != null)
            userLandPermissions.addAll(temp1);
        if(temp2 != null)
            userLandPermissions.addAll(temp2);

        if(userLandPermissions.size() > 0)
            db.sharedLandDao().deleteAll(userLandPermissions);
    }
}
