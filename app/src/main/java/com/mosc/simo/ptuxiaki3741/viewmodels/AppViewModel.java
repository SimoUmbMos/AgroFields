package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.AppRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppViewModel extends AndroidViewModel {
    public static final String TAG = "LandViewModel";

    private final AppRepository appRepository;

    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<LandZone>> landZones = new MutableLiveData<>();
    private final MutableLiveData<List<LandDataRecord>> landsHistory = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application.getApplicationContext());
        appRepository = new AppRepositoryImpl(db);
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

    public void init(){
        AsyncTask.execute(this::populateLists);
    }

    private void populateLists() {
        populateLands();
        populateLandZones();
        populateLandsRecords();
    }
    private void populateLands() {
        List<Land> landList;
        if(lands.getValue() != null){
            landList = lands.getValue();
            landList.clear();
            landList.addAll(appRepository.getLands());
        }else{
            landList = new ArrayList<>(appRepository.getLands());
        }
        lands.postValue(landList);
    }
    private void populateLandZones(){
        List<LandZone> zoneList;
        if(landZones.getValue() != null){
            zoneList = landZones.getValue();
            zoneList.clear();
            zoneList.addAll(appRepository.getLandZones());
        }else{
            zoneList = new ArrayList<>(appRepository.getLandZones());
        }
        landZones.postValue(zoneList);
    }
    private void populateLandsRecords() {
        List<LandDataRecord> landsHistoryList;
        if(landsHistory.getValue() != null){
            landsHistoryList = landsHistory.getValue();
            landsHistoryList.clear();
            landsHistoryList.addAll(appRepository.getLandRecords());
        }else{
            landsHistoryList = new ArrayList<>(appRepository.getLandRecords());
        }
        landsHistory.postValue(landsHistoryList);
    }

    public void saveLand(Land land){
        if(land != null){
            AsyncTask.execute(()->{
                LandDBAction action = LandDBAction.CREATE;
                if(land.getData().getId() != -1){
                    if(appRepository.getLandByID(land.getData().getId()) != null){
                        action = LandDBAction.UPDATE;
                    }
                }
                appRepository.saveLand(land);
                LandDataRecord landRecord = new LandDataRecord(
                        land.getData(),
                        action,
                        new Date()
                );
                appRepository.saveLandRecord(landRecord);
                List<LandZone> zones =appRepository.getLandZonesByLID(land.getData().getId());
                if(zones != null){
                    for(LandZone zone:zones){
                        if(MapUtil.notContains(
                                zone.getData().getBorder(),
                                land.getData().getBorder()
                        )){
                            appRepository.deleteZone(zone);
                        }else{
                            List<LatLng> tempBorders = MapUtil.intersection(
                                    zone.getData().getBorder(),
                                    land.getData().getBorder()
                            );
                            if(tempBorders.size()>0){
                                if(!ListUtils.arraysMatch(tempBorders,zone.getData().getBorder())){
                                    zone.getData().setBorder(tempBorders);
                                    appRepository.saveZone(zone);
                                }
                            }else{
                                appRepository.deleteZone(zone);
                            }
                        }
                    }
                }
                populateLists();
            });
        }
    }
    public void restoreLand(Land land) {
        if(land != null){
            AsyncTask.execute(()->{
                if(land.getData().getId() != -1){
                    LandDBAction action = LandDBAction.RESTORE;
                    appRepository.saveLand(land);
                    LandDataRecord landRecord = new LandDataRecord(
                            land.getData(),
                            action,
                            new Date()
                    );
                    appRepository.saveLandRecord(landRecord);
                }
                List<LandZone> zones =appRepository.getLandZonesByLID(land.getData().getId());
                if(zones != null){
                    if(zones.size()>0){
                        for(LandZone zone:zones){
                            if(MapUtil.notContains(
                                    zone.getData().getBorder(),
                                    land.getData().getBorder()
                            )){
                                appRepository.deleteZone(zone);
                            }
                        }
                    }
                }
                populateLists();
            });
        }
    }
    public void removeLand(Land land) {
        if(land != null){
            AsyncTask.execute(()-> {
                LandDataRecord landRecord = new LandDataRecord(
                        land.getData(),
                        LandDBAction.DELETE,
                        new Date()
                );
                appRepository.deleteZonesByLandID(land.getData().getId());
                appRepository.deleteLand(land);
                appRepository.saveLandRecord(landRecord);
                populateLists();
            });
        }
    }
    public void removeLands(List<Land> lands) {
        if(lands != null){
            AsyncTask.execute(()-> {
                LandDataRecord landRecord;
                for(Land land:lands){
                    landRecord = new LandDataRecord(
                            land.getData(),
                            LandDBAction.DELETE,
                            new Date()
                    );
                    appRepository.deleteZonesByLandID(land.getData().getId());
                    appRepository.deleteLand(land);
                    appRepository.saveLandRecord(landRecord);
                }
                populateLists();
            });
        }
    }

    public void saveZone(LandZone zone) {
        if(zone != null){
            appRepository.saveZone(zone);
            populateLists();
        }
    }
    public void removeZone(LandZone zone) {
        if(zone != null){
            AsyncTask.execute(()-> {
                appRepository.deleteZone(zone);
                populateLists();
            });
        }
    }
    public void removeZones(List<LandZone> zones) {
        if(zones != null){
            AsyncTask.execute(()-> {
                for(LandZone zone:zones){
                    appRepository.deleteZone(zone);
                }
                populateLists();
            });
        }
    }
}
