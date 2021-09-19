package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.enums.ViewModelStatus;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LandViewModel extends AndroidViewModel {
    public static final String TAG = "LandViewModel";
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<Land>> sharedLands = new MutableLiveData<>();
    private final MutableLiveData<List<LandDataRecord>> landsHistory = new MutableLiveData<>();
    private final MutableLiveData<ViewModelStatus> status = new MutableLiveData<>();
    private final LandRepositoryImpl landRepository;
    private User currUser = null;

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
    public MutableLiveData<ViewModelStatus> getStatus() {
        return status;
    }
    public LiveData<List<LandDataRecord>> getLandsHistory() {
        return landsHistory;
    }


    public void init(User user){
        currUser = user;
        backgroundLoad(user);
    }
    private void loadData(User currUser) {
        status.postValue(ViewModelStatus.LOADING);
        loadLands(currUser);
        status.postValue(ViewModelStatus.LOADED_LANDS);
        loadLandsRecords(currUser);
        status.postValue(ViewModelStatus.FULLY_LOADED);
    }
    private void backgroundLoad(User currUser) {
        AsyncTask.execute(()-> loadData(currUser));
    }

    private void loadLands(User user) {
        if(currUser != null){
            List<Land> landList, sharedLandList;
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
        if(currUser != null){
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

    public void saveLand(Land land, User user){
        status.postValue(ViewModelStatus.LOADING);
        lands.postValue(new ArrayList<>());
        sharedLands.postValue(new ArrayList<>());
        landsHistory.postValue(new ArrayList<>());
        AsyncTask.execute(()->{
            LandDBAction action;
            if(land.getData().getId() != -1){
                action = LandDBAction.UPDATE;
                Land temp = landRepository.getLand(land.getData().getId());
                if(temp.getData().getCreator_id() != land.getData().getCreator_id()){
                    land.getData().setCreator_id(temp.getData().getCreator_id());
                }
            }else{
                action = LandDBAction.CREATE;
            }

            Land newLand = landRepository.saveLand(land);
            loadLands(this.currUser);
            status.postValue(ViewModelStatus.LOADED_LANDS);

            LandDataRecord landRecord = new LandDataRecord(
                    newLand.getData(),
                    user,
                    action,
                    new Date()
            );
            landRepository.saveLandRecord(landRecord);
            loadLandsRecords(this.currUser);
            status.postValue(ViewModelStatus.FULLY_LOADED);
        });
    }
    public void removeLands(List<Land> l, User u) {
        if(u != null){
            status.postValue(ViewModelStatus.LOADING);
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
                        if(removeLand.getData().getCreator_id() == u.getId()){
                            landRecord = new LandDataRecord(
                                    removeLand.getData(),
                                    u,
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
                    landRepository.removeSharedLand(removeLand,u);
                }
                loadLands(this.currUser);
                status.postValue(ViewModelStatus.LOADED_LANDS);

                for(LandDataRecord temp:landRecords){
                    landRepository.saveLandRecord(temp);
                }
                loadLandsRecords(this.currUser);
                status.postValue(ViewModelStatus.FULLY_LOADED);
            });
        }
    }

    public void addSharedLand(Land l, User u){
        if(u != null){
            status.postValue(ViewModelStatus.LOADING);
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
            AsyncTask.execute(()->{
                landRepository.addSharedLand(l,u);

                loadLands(this.currUser);
                status.postValue(ViewModelStatus.LOADED_LANDS);
                loadLandsRecords(this.currUser);
                status.postValue(ViewModelStatus.FULLY_LOADED);
            });
        }
    }
    public void removeSharedLand(Land l, User u){
        if(u != null){
            status.postValue(ViewModelStatus.LOADING);
            lands.postValue(new ArrayList<>());
            sharedLands.postValue(new ArrayList<>());
            landsHistory.postValue(new ArrayList<>());
            AsyncTask.execute(()->{
                landRepository.removeSharedLand(l,u);

                loadLands(this.currUser);
                status.postValue(ViewModelStatus.LOADED_LANDS);
                loadLandsRecords(this.currUser);
                status.postValue(ViewModelStatus.FULLY_LOADED);
            });
        }
    }
}
