package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.UserLandPermissions;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LandViewModel extends AndroidViewModel {
    public static final String TAG = "LandViewModel";

    private final LandRepositoryImpl landRepository;
    private User currUser = null;

    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<LandZone>> landZones = new MutableLiveData<>();
    private final MutableLiveData<List<LandDataRecord>> landsHistory = new MutableLiveData<>();

    public LandViewModel(@NonNull Application application) {
        super(application);
        landRepository = new LandRepositoryImpl(
                MainActivity.getRoomDb(application.getApplicationContext())
        );
    }

    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public LiveData<List<LandZone>> getLandZones(){
        return landZones;
    }
    public LiveData<List<LandDataRecord>> getLandsHistory() {
        return landsHistory;
    }

    private void populateLands(User user) {
        if(user != null){
            List<Land> landList;
            if(lands.getValue() != null){
                landList = lands.getValue();
                landList.clear();
                landList.addAll(landRepository.getLandsByUser(user));
            }else{
                landList = new ArrayList<>(landRepository.getLandsByUser(user));
            }
            lands.postValue(landList);
        }else{
            clearLands();
        }
    }
    private void clearLands() {
        lands.postValue(new ArrayList<>());
    }

    private void populateLandZones(User user){
        if(user != null){
            List<LandZone> zoneList;
            if(landZones.getValue() != null){
                zoneList = landZones.getValue();
                zoneList.clear();
                zoneList.addAll(landRepository.getLandZonesByUser(user));
            }else{
                zoneList = new ArrayList<>(landRepository.getLandZonesByUser(user));
            }
            landZones.postValue(zoneList);
        }else{
            clearLandZones();
        }
    }
    private void clearLandZones() {
        landZones.postValue(new ArrayList<>());
    }

    private void populateLandsRecords(User user) {
        if(user != null){
            List<LandDataRecord> landsHistoryList;
            if(landsHistory.getValue() != null){
                landsHistoryList = landsHistory.getValue();
                landsHistoryList.clear();
                landsHistoryList.addAll(landRepository.getLandRecordsByUser(user));
            }else{
                landsHistoryList = new ArrayList<>(landRepository.getLandRecordsByUser(user));
            }
            landsHistory.postValue(landsHistoryList);
        }else{
            clearLandsRecords();
        }
    }
    private void clearLandsRecords() {
        landsHistory.postValue(new ArrayList<>());
    }

    private void populateLists(User user) {
        if(user != null){
            populateLands(user);
            populateLandZones(user);
            populateLandsRecords(user);
        }else{
            clearLists();
        }
    }
    private void clearLists() {
        clearLands();
        clearLandZones();
        clearLandsRecords();
    }

    public void init(User user){
        currUser = user;
        populateLists(user);
    }

    public void saveLand(Land land){
        if(currUser != null && land != null){
            clearLists();
            AsyncTask.execute(()->{
                LandDBAction action;
                if(land.getData().getId() != -1){
                    action = LandDBAction.UPDATE;
                }else{
                    action = LandDBAction.CREATE;
                }
                Land newLand = landRepository.saveLand(land);
                LandDataRecord landRecord = new LandDataRecord(
                        newLand.getData(),
                        currUser,
                        action,
                        new Date()
                );
                landRepository.saveLandRecord(landRecord);
                populateLists(currUser);
            });
        }
    }
    public void restoreLand(Land land) {
        if(currUser != null && land != null){
            clearLists();
            AsyncTask.execute(()->{
                if(land.getData().getId() != -1){
                    LandDBAction action = LandDBAction.RESTORE;
                    Land newLand = landRepository.saveLand(land);
                    LandDataRecord landRecord = new LandDataRecord(
                            newLand.getData(),
                            currUser,
                            action,
                            new Date()
                    );
                    landRepository.saveLandRecord(landRecord);
                }

                populateLists(currUser);
            });
        }
    }
    public void removeLand(Land removeLand) {
        if(currUser != null && removeLand != null){
            clearLists();
            AsyncTask.execute(()-> {
                boolean isShared = false;
                LandDataRecord landRecord;
                List<LandDataRecord> landRecords = new ArrayList<>();
                if(removeLand.getData() != null){
                    if(removeLand.getData().getCreator_id() == currUser.getId()){
                        landRecord = new LandDataRecord(
                                removeLand.getData(),
                                currUser,
                                LandDBAction.DELETE,
                                new Date()
                        );
                        landRecords.add(landRecord);
                    }else{
                        isShared = true;
                    }
                }
                if(isShared){
                    landRepository.removeLandPermissions(currUser, removeLand.getData());
                }else{
                    landRepository.deleteLand(removeLand);
                }
                for(LandDataRecord temp:landRecords){
                    landRepository.saveLandRecord(temp);
                }
                populateLists(currUser);
            });
        }
    }
    public void removeLands(List<Land> l) {
        if(currUser != null && l != null){
            clearLists();
            AsyncTask.execute(()-> {
                List<Land> removeLands = new ArrayList<>();
                List<Land> removeSharedLands = new ArrayList<>();
                LandDataRecord landRecord;
                List<LandDataRecord> landRecords = new ArrayList<>();
                for(Land removeLand:l){
                    if(removeLand.getData() != null){
                        if(removeLand.getData().getCreator_id() == currUser.getId()){
                            landRecord = new LandDataRecord(
                                    removeLand.getData(),
                                    currUser,
                                    LandDBAction.DELETE,
                                    new Date()
                            );
                            landRecords.add(landRecord);
                            removeLands.add(removeLand);
                        }else{
                            removeSharedLands.add(removeLand);
                        }
                    }
                }
                for(Land removeLand:removeLands){
                    landRepository.deleteLand(removeLand);
                }
                for(Land removeLand:removeSharedLands){
                    landRepository.removeLandPermissions(currUser, removeLand.getData());
                }
                for(LandDataRecord temp:landRecords){
                    landRepository.saveLandRecord(temp);
                }
                populateLists(currUser);
            });
        }
    }

    public void changeLandOwner(User contact, Land land, ActionResult<Boolean> result){
        if(currUser != null && land != null && contact != null){
            if(land.getData() != null){
                if(land.getData().getCreator_id() == currUser.getId()){
                    AsyncTask.execute(()->{
                        boolean r = landRepository.setLandNewCreator(
                                contact.getId(),
                                currUser.getId(),
                                land.getData().getId()
                        );
                        if(r){
                            clearLists();
                            populateLists(currUser);
                        }
                        result.onActionResult(r);
                    });
                }
            }
        }
    }
    public UserLandPermissions getLandPermissionForUser(User contact, Land land) {
        return landRepository.getLandPermissionsForUser(contact,land);
    }
    public void updateLandPermissions(UserLandPermissions perms){
        if(currUser != null){
            clearLists();
            AsyncTask.execute(()->{
                landRepository.addLandPermissions(perms);
                populateLists(currUser);
            });
        }
    }
    public void removeAllLandPermissions(User contact) {
        if(contact != null && currUser != null){
            clearLists();
            AsyncTask.execute(()->{
                landRepository.removeAllLandPermissions(currUser, contact);
                populateLists(currUser);
            });
        }
    }
}
