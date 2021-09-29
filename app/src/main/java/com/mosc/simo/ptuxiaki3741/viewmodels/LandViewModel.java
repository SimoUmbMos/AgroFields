package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandWithShare;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
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
    private final MutableLiveData<List<LandWithShare>> sharedLands = new MutableLiveData<>();
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
    public LiveData<List<LandWithShare>> getSharedLands(){
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
        loadLandsRecords(user);
    }
    private void backgroundLoad(User user) {
        AsyncTask.execute(()-> loadData(user));
    }

    private void loadLands(User user) {
        if(user != null){
            List<Land> landList;
            List<LandWithShare> sharedLandList;
            if(lands.getValue() != null){
                landList = lands.getValue();
                landList.clear();
                landList.addAll(landRepository.searchLandsByUser(user));
            }else{
                landList = new ArrayList<>(landRepository.searchLandsByUser(user));
            }
            landList.addAll(landRepository.getSharedLands(user));
            lands.postValue(landList);
            if(sharedLands.getValue() != null){
                sharedLandList = sharedLands.getValue();
                sharedLandList.clear();
                sharedLandList.addAll(landRepository.getSharedLandsToOtherUsers(user));
            }else{
                sharedLandList = new ArrayList<>(landRepository.getSharedLandsToOtherUsers(user));
            }
            sharedLands.postValue(sharedLandList);
        }else{
            clearLands();
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
                    landRepository.removeSharedLand(currUser, removeLand.getData());
                }else{
                    landRepository.deleteLand(removeLand);
                }
                loadLands(currUser);
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
                for(Land removeLand:removeSharedLands){
                    landRepository.removeSharedLand(currUser, removeLand.getData());
                }
                loadLands(currUser);
                for(LandDataRecord temp:landRecords){
                    landRepository.saveLandRecord(temp);
                }
                loadLandsRecords(currUser);
            });
        }
    }

    public void addSharedLand(LandData landData, User contact){
        if(contact != null && landData != null && currUser != null){
            if(landData.getCreator_id() == currUser.getId()){
                lands.postValue(new ArrayList<>());
                sharedLands.postValue(new ArrayList<>());
                landsHistory.postValue(new ArrayList<>());
                AsyncTask.execute(()->{
                    landRepository.addSharedLand(contact, landData);
                    loadLands(currUser);
                    loadLandsRecords(currUser);
                });
            }
        }
    }
    public void removeSharedLand(LandData landData, User contact){
        if(contact != null && landData != null && currUser != null){
            if(landData.getCreator_id() == currUser.getId()){
                lands.postValue(new ArrayList<>());
                sharedLands.postValue(new ArrayList<>());
                landsHistory.postValue(new ArrayList<>());
                AsyncTask.execute(()->{
                    landRepository.removeSharedLand(contact,landData);
                    loadLands(currUser);
                    loadLandsRecords(currUser);
                });
            }
        }
    }

    public void removeAllSharedLandsWithUser(User contact) {
        if(contact != null && currUser != null){
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
            AsyncTask.execute(()->{
                landRepository.removeAllSharedLands(currUser, contact);

                loadLands(currUser);
                loadLandsRecords(currUser);
            });
        }
    }
}
