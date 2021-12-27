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
import com.mosc.simo.ptuxiaki3741.util.MapUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppViewModel extends AndroidViewModel {
    public static final String TAG = "LandViewModel";

    private final AppRepository appRepository;

    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<Map<Long,List<LandZone>>> landZones = new MutableLiveData<>();
    private final MutableLiveData<List<LandDataRecord>> landsHistory = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application.getApplicationContext());
        appRepository = new AppRepositoryImpl(db);
    }

    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public LiveData<Map<Long,List<LandZone>>> getLandZones(){
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
        Map<Long,List<LandZone>> zoneList;
        if(landZones.getValue() != null){
            zoneList = landZones.getValue();
            zoneList.clear();
            zoneList.putAll(appRepository.getLandZones());
        }else{
            zoneList = new HashMap<>(appRepository.getLandZones());
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
            LandDBAction action = LandDBAction.CREATE;
            if(land.getData().getId() != 0){
                if(appRepository.getLandByID(land.getData().getId()) != null){
                    action = LandDBAction.UPDATE;
                }
            }
            List<LandZone> zones = appRepository.getLandZonesByLID(land.getData().getId());
            appRepository.saveLand(land);
            LandDataRecord landRecord = new LandDataRecord(
                    land.getData(),
                    action,
                    new Date()
            );
            appRepository.saveLandRecord(landRecord);
            if(zones != null){
                for(LandZone zone:zones){
                    if(!MapUtil.notContains(
                            zone.getData().getBorder(),
                            land.getData().getBorder()
                    )){
                        List<LatLng> tempBorders = MapUtil.intersection(
                                zone.getData().getBorder(),
                                land.getData().getBorder()
                        );
                        if(tempBorders.size()>0){
                            zone.getData().setBorder(tempBorders);
                            appRepository.saveZone(zone);
                        }
                    }
                }
            }
            populateLists();
        }
    }
    public void restoreLand(Land land) {
        if(land != null){
            if(land.getData().getId() != 0){
                List<LandZone> zones =appRepository.getLandZonesByLID(land.getData().getId());
                appRepository.saveLand(land);
                LandDataRecord landRecord = new LandDataRecord(
                        land.getData(),
                        LandDBAction.RESTORE,
                        new Date()
                );
                appRepository.saveLandRecord(landRecord);
                if(zones != null){
                    for(LandZone zone:zones){
                        if(!MapUtil.notContains(
                                zone.getData().getBorder(),
                                land.getData().getBorder()
                        )){
                            List<LatLng> tempBorders = MapUtil.intersection(
                                    zone.getData().getBorder(),
                                    land.getData().getBorder()
                            );
                            if(tempBorders.size()>0){
                                zone.getData().setBorder(tempBorders);
                                appRepository.saveZone(zone);
                            }
                        }
                    }
                }
            }
            populateLists();
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
                appRepository.saveLandRecord(landRecord);
                appRepository.deleteLand(land);
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
                    appRepository.saveLandRecord(landRecord);
                    appRepository.deleteLand(land);
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

    public void importLandsAndZones(List<Land> lands, List<LandZone> zones) {
        if(lands != null){
            LandDataRecord landRecord;
            for(Land land:lands){
                appRepository.saveLand(land);
                landRecord = new LandDataRecord(
                        land.getData(),
                        LandDBAction.IMPORTED,
                        new Date()
                );
                appRepository.saveLandRecord(landRecord);
            }
        }
        if(zones != null){
            Land temp;
            for(LandZone zone : zones){
                temp = appRepository.getLandByID(zone.getData().getLid());
                if(temp != null){
                    appRepository.saveZone(zone);
                }
            }
        }
        populateLists();
    }
}
