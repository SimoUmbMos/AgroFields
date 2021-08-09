package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

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
import java.util.Calendar;
import java.util.List;

public class LandViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<LandRecord>> landsHistory = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> selectedList = new MutableLiveData<>();
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

    public boolean isAllSelected() {
        return isAllSelected;
    }

    public void init(User user){
        currUser = user;
        if(lands.getValue() == null){
            lands.postValue(new ArrayList<>());
        }
        if(landsHistory.getValue() == null){
            landsHistory.postValue(new ArrayList<>());
        }
        if(selectedList.getValue() == null){
            selectedList.postValue(new ArrayList<>());
        }
        loadFullData(user);
    }
    private void loadLands(User user) {
        AsyncTask.execute(()->{
            List<Land> landList = new ArrayList<>(landRepository.searchLandsByUser(user));
            lands.postValue(landList);
        });
    }
    private void loadLandsRecords(User user) {
        AsyncTask.execute(()->{
            List<LandRecord> landsHistoryList = new ArrayList<>(landRepository.getLandRecordsByUser(user));
            landsHistory.postValue(landsHistoryList);
        });
    }
    private void initSelectedList(){
        List<Integer> selectedIndexes = new ArrayList<>();
        if(selectedList.getValue() != null){
            selectedIndexes.addAll(selectedList.getValue());
        }
        selectedList.postValue(selectedIndexes);
    }

    private void loadFullData(User currUser) {
        if(currUser != null){
            loadLands(currUser);
            loadLandsRecords(currUser);
            initSelectedList();
        }else{
            List<Land> landList = new ArrayList<>();
            List<LandRecord> landsHistoryList = new ArrayList<>();
            List<Integer> selectedIndexes = new ArrayList<>();
            lands.postValue(landList);
            landsHistory.postValue(landsHistoryList);
            selectedList.postValue(selectedIndexes);
        }
    }
    private void loadData(User currUser) {
        if(currUser != null){
            loadLands(currUser);
            loadLandsRecords(currUser);
        }else{
            List<Land> landList = new ArrayList<>();
            List<LandRecord> landsHistoryList = new ArrayList<>();
            lands.postValue(landList);
            landsHistory.postValue(landsHistoryList);
        }
    }

    public void saveLand(Land land, User user){
        AsyncTask.execute(()->{
            List<LandPoint> landPoints = new ArrayList<>(land.getBorder());
            Land newLand = landRepository.saveLand(land);
            int index = indexOfLand(newLand);
            LandDBAction action;
            if(index < 0){
                action = LandDBAction.CREATE;
            }else{
                action = LandDBAction.UPDATE;
            }
            LandRecord landRecord = new LandRecord(
                    newLand.getData(),
                    landPoints,
                    user,
                    action,
                    Calendar.getInstance().getTime()
            );
            landRepository.saveLandRecord(landRecord);
            if(this.currUser != null)
                loadData(this.currUser);
        });
    }
    public void removeLands(List<Land> removeLandList, User user) {
        AsyncTask.execute(()-> {
            LandRecord landRecord;
            for(Land removeLand:removeLandList){
                landRecord = new LandRecord(
                        removeLand,
                        user,
                        LandDBAction.DELETE,
                        Calendar.getInstance().getTime()
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
        for(int i = 0; i < landSize(); i++){
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
        isAllSelected = (indexes.size() == landSize());
        selectedList.postValue(indexes);
    }

    public List<Land> getLandsList() {
        List<Land> lands = this.lands.getValue();
        if(lands != null)
            return lands;
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
    public int landSize() {
        return getLandsList().size();
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
    public void removeSelectedLands(User user) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            removeLands(returnSelectedLands(),user);
        }
    }
    private int indexOfLand(Land land) {
        List<Land> lands = getLandsList();
        for(int i = 0; i < lands.size();i++){
            if(Land.equals(lands.get(i),land)){
                return i;
            }
        }
        return -1;
    }

}
