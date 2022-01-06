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
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.models.entities.TagData;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.AppRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.util.MapUtil;

import java.time.LocalDate;
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
    private final MutableLiveData<List<TagData>> tags = new MutableLiveData<>();
    private final MutableLiveData<Map<LocalDate,List<CalendarNotification>>> notifications = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application);
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
    public LiveData<List<TagData>> getTags() {
        return tags;
    }
    public LiveData<Map<LocalDate,List<CalendarNotification>>> getNotifications() {
        return notifications;
    }

    public void init(){
        AsyncTask.execute(this::populateLists);
    }

    private void populateLists() {
        populateLands();
        populateLandZones();
        populateLandsRecords();
        populateTags();
        populateNotifications();
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
    private void populateTags() {
        List<TagData> tagsList;
        if(tags.getValue() != null){
            tagsList = tags.getValue();
            tagsList.clear();
            tagsList.addAll(appRepository.getTags());
        }else{
            tagsList = new ArrayList<>(appRepository.getTags());
        }
        tags.postValue(tagsList);
    }
    private void populateNotifications() {
        Map<LocalDate,List<CalendarNotification>> notificationsList;
        if(notifications.getValue() != null){
            notificationsList = notifications.getValue();
            notificationsList.clear();
            notificationsList.putAll(appRepository.getNotifications());
        }else{
            notificationsList = new HashMap<>(appRepository.getNotifications());
        }
        notifications.postValue(notificationsList);
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

            List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(land.getData().getBorder());
            List<List<LatLng>> tempHoles = new ArrayList<>();
            for(List<LatLng> hole : land.getData().getHoles()){
                tempHoles.add(DataUtil.removeSamePointStartEnd(hole));
            }
            land.getData().setBorder(tempPointList);
            land.getData().setHoles(tempHoles);

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
            List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(zone.getData().getBorder());
            zone.getData().setBorder(tempPointList);
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

                List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(land.getData().getBorder());
                land.getData().setBorder(tempPointList);

                List<List<LatLng>> tempHoles = new ArrayList<>();
                for(List<LatLng> hole : land.getData().getHoles()){
                    tempHoles.add(DataUtil.removeSamePointStartEnd(hole));
                }
                land.getData().setHoles(tempHoles);

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
                    List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(zone.getData().getBorder());
                    zone.getData().setBorder(tempPointList);
                    appRepository.saveZone(zone);
                }
            }
        }
        populateLists();
    }

    public void saveTag(TagData tag) {
        if(tag != null){
            AsyncTask.execute(()-> {
                appRepository.saveTag(tag);
                populateLists();
            });
        }
    }
    public void removeTag(TagData tag) {
        if(tag != null){
            AsyncTask.execute(()-> {
                appRepository.deleteTag(tag);
                populateLists();
            });
        }
    }

    public void saveNotification(CalendarNotification notification) {
        if(notification != null){
            AsyncTask.execute(()-> {
                if(notification.getId() != 0){
                    DataUtil.removeNotificationToAlertManager(
                            getApplication().getApplicationContext(),
                            appRepository.getNotification(notification.getId())
                    );
                }
                appRepository.saveNotification(notification);
                populateLists();
                DataUtil.addNotificationToAlertManager(
                        getApplication().getApplicationContext(),
                        notification
                );
            });
        }
    }
    public void removeNotification(CalendarNotification notification) {
        if(notification != null){
            AsyncTask.execute(()-> {
                DataUtil.removeNotificationToAlertManager(
                        getApplication().getApplicationContext(),
                        notification
                );
                appRepository.deleteNotification(notification);
                populateLists();
            });
        }
    }
}
