package com.mosc.simo.ptuxiaki3741.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mosc.simo.ptuxiaki3741.database.repositorys.LandRepository;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.database.model.User;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

public class LandViewModel extends ViewModel {
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> selectedList = new MutableLiveData<>();
    private boolean isInit = false;

    public boolean isInit() {
        return isInit;
    }

    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public void addLand(Land land){
        List<Land> landList = lands.getValue();
        if(landList != null){
            add(land, landList);
        }
    }
    public void editLand(Land land) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            edit(land,landList);
        }
    }
    public void removeLand(Land land) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            remove(land,landList);
        }
    }
    public void removeLand(int index) {
        List<Land> landList = lands.getValue();
        if(landList != null){
            remove(index,landList);
        }
    }
    private void add(Land land, List<Land> landList) {
        landList.add(land);
        lands.postValue(landList);
    }
    private void edit(Land land, List<Land> landList) {
        int index = -1;
        for(Land tempLand : landList){
            if(tempLand.getId() == land.getId()){
                index = landList.indexOf(tempLand);
            }
        }
        if(index != -1){
            landList.set(index,land);
            lands.postValue(landList);
        }
    }
    private void remove(Land land, List<Land> landList) {
        if(landList.contains(land)){
            landList.remove(land);
            lands.postValue(landList);
        }else{
            int index = -1;
            for(Land tempLand : landList){
                if(tempLand.getId() == land.getId()){
                    index = landList.indexOf(tempLand);
                }
            }
            if(index != -1){
                landList.remove(index);
                lands.postValue(landList);
            }
        }
    }
    private void remove(int index, List<Land> landList) {
        if(index > -1 && index < size()){
            landList.remove(index);
            lands.postValue(landList);
        }
    }

    public LiveData<List<Integer>> getSelectedLands() {
        return selectedList;
    }

    private void selectAllLands() {
//        todo select All Action
    }
    private void deselectAllLands() {
//        todo deselect All Action
    }
    private void toggleSelectLands(int position) {
//        todo toggle Select Action
    }

    public void init(User user, LandRepository helper){
        isInit = true;
        initLands(user,helper);
    }
    private void initLands(User user, LandRepository helper) {
        List<Land> landList = lands.getValue();
        if(landList == null){
            landList = new ArrayList<>();
        }
        loadLands(user, helper,landList);
        lands.postValue(landList);
    }
    private void loadLands(User user, LandRepository helper,List<Land> landList) {
        landList.clear();
        if(helper != null){
            landList.addAll(helper.getLands(user.getId()));
        }
    }

    public int size() {
        int size = 0;
        if(lands.getValue() != null)
            size = lands.getValue().size();
        return size;
    }
}
