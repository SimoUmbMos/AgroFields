package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.ArrayList;
import java.util.List;

public class LandViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> selectedList = new MutableLiveData<>();
    private final LandRepositoryImpl landRepository;
    private boolean isInit = false;
    private boolean isAllSelected = false;

    public LandViewModel(@NonNull Application application) {
        super(application);
        landRepository = new LandRepositoryImpl(MainActivity.getDb(application));
    }

    public boolean isInit() {
        return isInit;
    }
    public boolean isAllSelected() {
        return isAllSelected;
    }

    public void init(User user){
        isInit = true;
        loadLands(user);
        initSelectedList();
    }
    private void loadLands(User user) {
        final List<Land> landList = new ArrayList<>();
        AsyncTask.execute(() -> {
            //TODO: LOAD ALL LAND
            lands.postValue(landList);
        });
    }
    private void initSelectedList(){
        List<Integer> selectedIndexes = selectedList.getValue();
        if(selectedIndexes == null){
            selectedIndexes = new ArrayList<>();
        }
        selectedList.postValue(selectedIndexes);
    }

    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public Land addLand(Land newLand){
        List<Land> lands = getLandsList();
        //TODO: SAVE LAND TO DB
        lands.add(newLand);
        this.lands.postValue(lands);
        return newLand;
    }
    public void editLand(Land editedLand) {
        //TODO: EDIT LAND TO LIST + DB
    }
    public void removeLand(Land removeLand) {
        List<Land> lands = getLandsList();
        //TODO: REMOVE LAND FROM DB
        lands.remove(removeLand);
        this.lands.postValue(lands);
    }
    public void removeLands(List<Land> removeLandList) {
        List<Land> lands = getLandsList();
        //TODO: REMOVE LANDS FROM DB
        lands.removeAll(removeLandList);
        this.lands.postValue(lands);
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
    public void removeSelectedLands() {
        List<Land> landList = lands.getValue();
        if(landList != null){
            removeLands(returnSelectedLands());
        }
    }
}
