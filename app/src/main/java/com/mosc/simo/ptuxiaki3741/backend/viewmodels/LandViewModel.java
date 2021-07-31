package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandHistoryRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.LandPoint;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LandViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> selectedList = new MutableLiveData<>();
    private final LandRepositoryImpl landRepository;
    private final LandHistoryRepositoryImpl landHistoryRepository;
    private boolean isAllSelected = false;

    public LandViewModel(@NonNull Application application) {
        super(application);
        landRepository = new LandRepositoryImpl(
                MainActivity.getRoomDb(application.getApplicationContext())
        );
        landHistoryRepository = new LandHistoryRepositoryImpl(
                MainActivity.getRoomDb(application.getApplicationContext())
        );
    }

    public boolean isAllSelected() {
        return isAllSelected;
    }

    public void init(User user){
        if(lands.getValue() == null){
            lands.postValue(new ArrayList<>());
        }
        if(selectedList.getValue() == null){
            selectedList.postValue(new ArrayList<>());
        }
        List<Land> landList = new ArrayList<>();
        List<Integer> selectedIndexes = new ArrayList<>();
        loadLands(user,landList);
        initSelectedList(selectedIndexes);
    }
    private void loadLands(User user, List<Land> landList) {
        AsyncTask.execute(()->{
            landList.clear();
            landList.addAll(landRepository.searchLandsByUser(user));
            lands.postValue(landList);
        });
    }
    private void initSelectedList(List<Integer> selectedIndexes){
        if(selectedList.getValue() != null){
            selectedIndexes.addAll(selectedList.getValue());
        }
        selectedList.postValue(selectedIndexes);
    }

    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public void saveLand(Land land, User currUser){
        AsyncTask.execute(()->{
            List<Land> lands = getLandsList();
            List<LandPoint> landPoints = new ArrayList<>(land.getBorder());
            Land newLand = landRepository.saveLand(land);
            int index = indexOfLand(newLand);
            LandDBAction action;
            if(index < 0){
                lands.add(newLand);
                action = LandDBAction.INSERT;
            }else{
                lands.set(index,newLand);
                action = LandDBAction.UPDATE;
            }
            LandRecord landRecord = new LandRecord(
                    newLand.getData(),
                    landPoints,
                    currUser,
                    action,
                    Calendar.getInstance().getTime()
            );
            landHistoryRepository.saveLandRecord(landRecord);
            this.lands.postValue(lands);
        });
    }
    public void removeLands(List<Land> removeLandList, User currUser) {
        AsyncTask.execute(()-> {
            List<Land> lands = getLandsList();
            LandRecord landRecord;
            for(Land removeLand:removeLandList){
                landRecord = new LandRecord(
                        removeLand,
                        currUser,
                        LandDBAction.DELETE,
                        Calendar.getInstance().getTime()
                );
                landHistoryRepository.saveLandRecord(landRecord);
                landRepository.deleteLand(removeLand);
            }
            lands.removeAll(removeLandList);
            this.lands.postValue(lands);
        });
    }

    public LiveData<List<Integer>> getSelectedLands() {
        return selectedList;
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
    public List<Integer> getSelectedIndexes() {
        List<Integer> selectedList = this.selectedList.getValue();
        if(selectedList != null)
            return selectedList;
        else
            return new ArrayList<>();
    }
    public int landSize() {
        return getLandsList().size();
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
    public void removeSelectedLands(User currUser) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            removeLands(returnSelectedLands(),currUser);
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

    public Land getLand(int position) {
        List<Land> lands = getLandsList();
        if(lands.size() > position && position > -1){
            return lands.get(position);
        }else{
            return null;
        }
    }
}
