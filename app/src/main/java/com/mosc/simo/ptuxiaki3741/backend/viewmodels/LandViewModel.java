package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LandViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<LandRecord>> landsHistory = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> selectedList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLands = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLandRecords = new MutableLiveData<>();
    private final LandRepositoryImpl landRepository;
    private boolean isAllSelected = false;
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
    public LiveData<List<Integer>> getSelectedLands() {
        return selectedList;
    }
    public LiveData<List<LandRecord>> getLandsHistory() {
        return landsHistory;
    }
    public MutableLiveData<Boolean> isLoadingLands() {
        return isLoadingLands;
    }
    public MutableLiveData<Boolean> isLoadingLandRecords() {
        return isLoadingLandRecords;
    }

    public boolean isAllSelected() {
        return isAllSelected;
    }

    public void init(User user){
        isLoadingLands.postValue(false);
        isLoadingLandRecords.postValue(false);
        currUser = user;
        loadFullData(user);
    }
    private void loadLands(User user) {
        AsyncTask.execute(()->{
            if(isLoadingLands.getValue() != null){
                if(!isLoadingLands.getValue())
                    isLoadingLands.postValue(true);
            }else{
                isLoadingLands.postValue(true);
            }
            List<Land> landList;
            if(lands.getValue() != null){
                landList = lands.getValue();
                landList.clear();
                landList.addAll(landRepository.searchLandsByUser(user));
            }else{
                landList = new ArrayList<>(landRepository.searchLandsByUser(user));
            }
            lands.postValue(landList);
            isLoadingLands.postValue(false);
        });
    }
    private void clearLands() {
        List<Land> landList;
        if(lands.getValue() != null){
            landList = lands.getValue();
            landList.clear();
        }else{
            landList = new ArrayList<>();
        }
        lands.postValue(landList);
    }
    private void loadLandsRecords(User user) {
        AsyncTask.execute(()->{
            if(isLoadingLandRecords.getValue() != null){
                if(!isLoadingLandRecords.getValue())
                    isLoadingLandRecords.postValue(true);
            }else{
                isLoadingLandRecords.postValue(true);
            }
            List<LandRecord> landsHistoryList;
            if(landsHistory.getValue() != null){
                landsHistoryList = landsHistory.getValue();
                landsHistoryList.clear();
                landsHistoryList.addAll(landRepository.getLandRecordsByUser(user));
            }else{
                landsHistoryList = new ArrayList<>(landRepository.getLandRecordsByUser(user));
            }
            landsHistory.postValue(landsHistoryList);
            isLoadingLandRecords.postValue(false);
        });
    }
    private void clearLandsRecords() {
        List<LandRecord> landsHistoryList;
        if(landsHistory.getValue() != null){
            landsHistoryList = landsHistory.getValue();
            landsHistoryList.clear();
        }else{
            landsHistoryList = new ArrayList<>();
        }
        landsHistory.postValue(landsHistoryList);
    }
    private void clearSelectedIndexes() {
        List<Integer> selectedIndexes;
        if (selectedList.getValue() != null) {
            selectedIndexes = selectedList.getValue();
            selectedIndexes.clear();
        } else {
            selectedIndexes = new ArrayList<>();
        }
        selectedList.postValue(selectedIndexes);
    }
    private void loadData(User currUser) {
        if(currUser != null){
            loadLands(currUser);
            loadLandsRecords(currUser);
        }else{
            clearLands();
            clearLandsRecords();
        }
    }
    private void loadFullData(User currUser) {
        loadData(currUser);
        clearSelectedIndexes();
    }

    public void saveLand(Land land, User user){
        isLoadingLands.setValue(true);
        isLoadingLandRecords.setValue(true);
        clearLands();
        clearLandsRecords();
        AsyncTask.execute(()->{
            List<LandPoint> landPoints = new ArrayList<>(land.getBorder());
            LandDBAction action;
            if(land.getData().getId() != -1){
                action = LandDBAction.UPDATE;
            }else{
                action = LandDBAction.CREATE;
            }
            Land newLand = landRepository.saveLand(land);
            LandRecord landRecord = new LandRecord(
                    newLand.getData(),
                    landPoints,
                    user,
                    action,
                    new Date()
            );
            landRepository.saveLandRecord(landRecord);

            loadData(this.currUser);
        });
    }
    public void removeLands(List<Land> removeLandList, User user) {
        clearLands();
        clearLandsRecords();
        AsyncTask.execute(()-> {
            LandRecord landRecord;
            for(Land removeLand:removeLandList){
                landRecord = new LandRecord(
                        removeLand,
                        user,
                        LandDBAction.DELETE,
                        new Date()
                );
                landRepository.saveLandRecord(landRecord);
                landRepository.deleteLand(removeLand);
            }
            loadData(this.currUser);
        });
    }

    public void selectAllLands() {
        List<Integer> indexes = selectedList.getValue();
        if(indexes != null ){
            indexes.clear();
        }else{
            indexes = new ArrayList<>();
        }
        for(int i = 0; i < getLandsList().size(); i++){
            indexes.add(i);
        }
        isAllSelected = true;
        selectedList.postValue(indexes);
    }
    public void deselectAllLands() {
        List<Integer> indexes = selectedList.getValue();
        if(indexes != null ){
            indexes.clear();
        }else{
            indexes = new ArrayList<>();
        }
        isAllSelected = false;
        selectedList.postValue(indexes);
    }
    public void toggleSelectOnPosition(int pos) {
        Integer position = pos;
        List<Integer> indexes = selectedList.getValue();
        if(indexes != null ){
            if(indexes.contains(position)){
                indexes.remove(position);
            }else{
                indexes.add(position);
            }
        }else{
            indexes=new ArrayList<>();
            indexes.add(position);
        }
        isAllSelected = (indexes.size() == getLandsList().size());
        selectedList.postValue(indexes);
    }

    public List<Land> getLandsList() {
        List<Land> lands = this.lands.getValue();
        if(lands != null)
            return lands;
        else
            return new ArrayList<>();
    }
    public List<LandRecord> getLandsHistoryList() {
        List<LandRecord> landHistory = this.landsHistory.getValue();
        if(landHistory != null)
            return landHistory;
        else
            return new ArrayList<>();
    }
    public Land getLand(int position) {
        List<Land> lands = getLandsList();
        if(lands.size() > position && position > -1){
            return lands.get(position);
        }else{
            return null;
        }
    }
    public List<Integer> getSelectedIndexes() {
        List<Integer> selectedList = this.selectedList.getValue();
        if(selectedList != null)
            return selectedList;
        else
            return new ArrayList<>();
    }
    public List<Land> returnSelectedLands(){
        List<Land> lands = getLandsList();
        List<Integer> indexes = getSelectedIndexes();
        List<Land> selectedLands = new ArrayList<>();

        for (Integer i : indexes){
            selectedLands.add(lands.get(i));
        }

        return selectedLands;
    }
    public void removeSelectedLands() {
        List<Land> landList = lands.getValue();
        if(landList != null && currUser != null){
            removeLands(returnSelectedLands(),currUser);
        }
    }
}
