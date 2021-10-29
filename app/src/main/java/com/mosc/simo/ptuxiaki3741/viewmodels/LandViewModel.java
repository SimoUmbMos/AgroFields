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
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
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
    private final MutableLiveData<List<Land>> sharedLands = new MutableLiveData<>();
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
    public LiveData<List<Land>> getSharedLands(){
        return sharedLands;
    }
    public LiveData<List<LandDataRecord>> getLandsHistory() {
        return landsHistory;
    }


    public void init(User user){
        currUser = user;
        backgroundLoad(user);
    }
    private void loadData(User user) {
        loadLands(user);
        loadSharedLands(user);
        loadLandsRecords(user);
    }
    private void backgroundLoad(User user) {
        AsyncTask.execute(()-> loadData(user));
    }

    private void loadLands(User user) {
        if(user != null){
            List<Land> landList;
            if(lands.getValue() != null){
                landList = lands.getValue();
                landList.clear();
                landList.addAll(landRepository.searchLandsByUser(user));
            }else{
                landList = new ArrayList<>(landRepository.searchLandsByUser(user));
            }
            lands.postValue(landList);
        }else{
            clearLands();
        }
    }
    private void loadSharedLands(User user) {
        if(user != null){
            List<Land> sharedLandList;
            if(sharedLands.getValue() != null){
                sharedLandList = sharedLands.getValue();
                sharedLandList.clear();
                sharedLandList.addAll(landRepository.getSharedLands(user));
            }else{
                sharedLandList = new ArrayList<>(landRepository.getSharedLands(user));
            }
            sharedLands.postValue(sharedLandList);
        }else{
            clearSharedLands();
        }
    }
    private void loadLandsRecords(User user) {
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

    private void clearLands() {
        lands.postValue(new ArrayList<>());
    }
    private void clearSharedLands() {
        sharedLands.postValue(new ArrayList<>());
    }
    private void clearLandsRecords() {
        landsHistory.postValue(new ArrayList<>());
    }

    public void saveLand(Land land){
        if(currUser != null && land != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
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
                loadLands(currUser);
                loadSharedLands(currUser);
                loadLandsRecords(currUser);
            });
        }
    }
    public void restoreLand(Land land) {
        if(currUser != null && land != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
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

                loadLands(currUser);
                loadSharedLands(currUser);
                loadLandsRecords(currUser);
            });
        }
    }
    public void removeLand(Land removeLand) {
        if(currUser != null && removeLand != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
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
                loadLands(currUser);
                loadSharedLands(currUser);
                for(LandDataRecord temp:landRecords){
                    landRepository.saveLandRecord(temp);
                }
                loadLandsRecords(currUser);
            });
        }
    }
    public void removeLands(List<Land> l) {
        if(currUser != null && l != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
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
                loadLands(currUser);
                for(Land removeLand:removeSharedLands){
                    landRepository.removeLandPermissions(currUser, removeLand.getData());
                }
                loadSharedLands(currUser);
                for(LandDataRecord temp:landRecords){
                    landRepository.saveLandRecord(temp);
                }
                loadLandsRecords(currUser);
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
                            lands.postValue(new ArrayList<>());
                            sharedLands.postValue(new ArrayList<>());
                            landsHistory.postValue(new ArrayList<>());
                            loadLands(currUser);
                            loadSharedLands(currUser);
                            loadLandsRecords(currUser);
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
    public void addLandPermissions(UserLandPermissions perms){
        if(perms != null && currUser != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
            AsyncTask.execute(()->{
                landRepository.addLandPermissions(perms);
                loadLands(currUser);
                loadSharedLands(currUser);
                loadLandsRecords(currUser);
            });
        }
    }
    public void removeLandPermissions(LandData landData, User contact){
        if(contact != null && landData != null && currUser != null){
            if(landData.getCreator_id() == currUser.getId()){
                lands.postValue(new ArrayList<>());
                sharedLands.postValue(new ArrayList<>());
                landsHistory.postValue(new ArrayList<>());
                AsyncTask.execute(()->{
                    landRepository.removeLandPermissions(contact,landData);
                    loadLands(currUser);
                    loadSharedLands(currUser);
                    loadLandsRecords(currUser);
                });
            }
        }
    }
    public void removeAllLandPermissions(User contact) {
        if(contact != null && currUser != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
            AsyncTask.execute(()->{
                landRepository.removeAllLandPermissions(currUser, contact);
                loadLands(currUser);
                loadSharedLands(currUser);
                loadLandsRecords(currUser);
            });
        }
    }
}
