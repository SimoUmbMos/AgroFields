package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mosc.simo.ptuxiaki3741.database.model.LandPoint;
import com.mosc.simo.ptuxiaki3741.database.repositorys.LandRepository;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.User;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LandViewModel extends ViewModel {
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> selectedList = new MutableLiveData<>();
    private final HashMap<Long, List<LandPoint>> landPoints = new HashMap<>();
    private boolean isInit = false;
    private boolean isAllSelected = false;

    public boolean isInit() {
        return isInit;
    }
    public boolean isAllSelected() {
        return isAllSelected;
    }

    public void init(User user, LandRepository helper){
        initLands(user,helper);
    }
    private void initLands(User user, LandRepository helper) {
        isInit = true;
        List<Land> landList = lands.getValue();
        if(landList == null){
            landList = new ArrayList<>();
        }
        List<Integer> selectedIndexes = selectedList.getValue();
        if(selectedIndexes == null){
            selectedIndexes = new ArrayList<>();
        }
        loadLands(user, helper, landList, selectedIndexes);
    }
    private void loadLands(User user, LandRepository helper,
                           List<Land> landList, List<Integer> selectedIndexes) {
        landList.clear();
        if(helper != null){
            AsyncTask.execute(() -> {
                landList.addAll(helper.getLands(user.getId()));
                landPoints.clear();
                List<LandPoint> temp;
                for(Land land :landList){
                    temp = helper.getLandPoints(land.getId());
                    landPoints.put(land.getId(),temp);
                }
                lands.postValue(landList);
                selectedList.postValue(selectedIndexes);
            });
        }
    }

    public List<LandPoint> getLandPoints(long land_id) {
        if(landPoints.containsKey(land_id)){
            return landPoints.get(land_id);
        }else{
            return null;
        }
    }

    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public List<Land> returnSelectedLands(){
        List<Land> lands = this.lands.getValue();
        List<Integer> indexes = this.selectedList.getValue();
        List<Land> selectedLands = new ArrayList<>();

        if( indexes != null &&  lands != null){
            for (Integer i : indexes){
                selectedLands.add(lands.get(i));
            }
        }

        return selectedLands;
    }
    public void addLand(Land land, List<LandPoint> landPoints){
        List<Land> landList = lands.getValue();
        if(landList != null){
            add(land, landList, landPoints);
        }
    }
    public void editLand(Land land, List<LandPoint> landPoints) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            edit(land, landList, landPoints);
        }
    }
    public void removeLand(Land land, List<LandPoint> landPoints) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            remove(land, landList);
        }
    }
    public void removeSelectedLands() {
        List<Land> landList = lands.getValue();
        if(landList != null){
            List<Land> selectedLands = returnSelectedLands();
            for(Land land:selectedLands){
                remove(land,landList);
            }
        }
    }
    private void add(Land land, List<Land> landList, List<LandPoint> landPoints) {
        this.landPoints.put(land.getId(),landPoints);
        landList.add(land);
        lands.postValue(landList);
    }
    private void edit(Land land, List<Land> landList, List<LandPoint> landPoints) {
        int index = -1;
        for(Land tempLand : landList){
            if(tempLand.getId() == land.getId()){
                index = landList.indexOf(tempLand);
                break;
            }
        }
        if(index != -1){
            if(this.landPoints.containsKey(land.getId())){
                this.landPoints.replace(land.getId(), landPoints);
            }else{
                this.landPoints.put(land.getId(), landPoints);
            }
            landList.set(index,land);
            lands.postValue(landList);
        }
    }
    private void remove(Land land, List<Land> landList) {
        Land landDelete = null;
        if(landList.contains(land)){
            landDelete = land;
        }else{
            for(Land tempLand : landList){
                if(tempLand.getId() == land.getId()){
                    landDelete = tempLand;
                    break;
                }
            }
        }
        if(landDelete != null){
            landPoints.remove(landDelete.getId());
            landList.remove(landDelete);
            lands.postValue(landList);
        }
    }

    public LiveData<List<Integer>> getSelectedLands() {
        return selectedList;
    }
    public void selectAllLands() {
        List<Integer> indexes = selectedList.getValue();
        if(indexes != null ){
            indexes.clear();
            for(int i = 0; i < landSize(); i++){
                indexes.add(i);
            }
            isAllSelected = true;
            selectedList.postValue(indexes);
        }
    }
    public void deselectAllLands() {
        List<Integer> indexes = selectedList.getValue();
        if(indexes != null ){
            indexes.clear();
            isAllSelected = false;
            selectedList.postValue(indexes);
        }
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
            isAllSelected = (indexes.size() == landSize());
            selectedList.postValue(indexes);
        }
    }

    public int landSize() {
        int size = 0;
        if(lands.getValue() != null)
            size = lands.getValue().size();
        return size;
    }
}
