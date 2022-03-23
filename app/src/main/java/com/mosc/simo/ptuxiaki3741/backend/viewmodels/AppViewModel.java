package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.backend.room.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.data.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.repository.AppRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.backend.repository.AppRepository;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.MapUtil;
import com.mosc.simo.ptuxiaki3741.ui.applications.MainApplication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AppViewModel extends AndroidViewModel {
    public static final String TAG = "LandViewModel";
    private final AppRepository appRepository;

    private final MutableLiveData<List<Long>> snapshots = new MutableLiveData<>();
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<String>> landsTags = new MutableLiveData<>();
    private final MutableLiveData<Map<Long,List<LandZone>>> landZones = new MutableLiveData<>();
    private final MutableLiveData<List<LandHistoryRecord>> landsHistory = new MutableLiveData<>();

    private final MutableLiveData<List<CalendarCategory>> calendarCategories = new MutableLiveData<>();
    private final MutableLiveData<Map<LocalDate,List<CalendarEntity>>> notifications = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainApplication.getRoomDb(application);
        appRepository = new AppRepositoryImpl(db);
    }

    public LiveData<List<Long>> getSnapshots(){
        return snapshots;
    }
    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public LiveData<List<String>> getLandsTags(){
        return landsTags;
    }
    public LiveData<Map<Long,List<LandZone>>> getLandZones(){
        return landZones;
    }
    public LiveData<List<LandHistoryRecord>> getLandsHistory() {
        return landsHistory;
    }

    public LiveData<List<CalendarCategory>> getCalendarCategories(){return calendarCategories;}
    public LiveData<Map<LocalDate,List<CalendarEntity>>> getNotifications() {
        return notifications;
    }

    public void setDefaultSnapshot(long snapshot){
        appRepository.setDefaultSnapshot(snapshot);
        populateLists();
    }
    public long getDefaultSnapshot(){
        return appRepository.getDefaultSnapshot();
    }

    private void populateLists() {
        populateDataSnapshots();
        populateLandsTags();
        populateLands();
        populateLandZones();
        populateLandsRecords();
        populateCalendarCategories();
        populateNotifications();
    }
    private void populateSnapshotLists(){
        populateDataSnapshots();
        populateLands();
        populateLandsTags();
        populateLandZones();
        populateLandsRecords();
    }
    private void populateDataSnapshots() {
        List<Long> snapshotsList = appRepository.getSnapshots();
        if(snapshotsList == null)
            snapshotsList = new ArrayList<>();
        snapshots.postValue(snapshotsList);
    }
    private void populateLands() {
        List<Land> landList = appRepository.getLands();
        if(landList == null)
            landList = new ArrayList<>();
        lands.postValue(landList);
    }
    private void populateLandsTags() {
        List<String> tagsList = appRepository.getLandTags();
        if(tagsList == null)
            tagsList = new ArrayList<>();
        landsTags.postValue(tagsList);
    }
    private void populateLandZones(){
        Map<Long,List<LandZone>> zoneList = appRepository.getLandZones();
        if(zoneList == null)
            zoneList = new HashMap<>();
        landZones.postValue(zoneList);
    }
    private void populateLandsRecords() {
        List<LandHistoryRecord> landsHistoryList = appRepository.getLandRecords();
        if(landsHistoryList == null)
            landsHistoryList = new ArrayList<>();
        landsHistory.postValue(landsHistoryList);
    }
    private void populateCalendarCategories() {
        List<CalendarCategory> categories = appRepository.getCalendarCategories();
        if(categories == null)
            categories = new ArrayList<>();
        categories.add(0,getDefaultCategory());
        calendarCategories.postValue(categories);
    }
    private void populateNotifications() {
        Map<LocalDate,List<CalendarEntity>> notificationsList = appRepository.getNotifications();
        if(notificationsList == null)
            notificationsList = new TreeMap<>();
        CalendarCategory defaultCategory = getDefaultCategory();
        notificationsList.forEach((date,notifications)->{
            for(CalendarEntity entity : notifications){
                if(entity.getCategory() != null) continue;
                entity.setCategory(defaultCategory);
                entity.getNotification().setCategoryID(defaultCategory.getId());
            }
        });
        notifications.postValue(notificationsList);
    }

    public void saveLand(Land land){
        if(land == null) return;
        if(land.getData() == null) return;

        LandDBAction action = LandDBAction.CREATE;
        if(land.getData().getId() != 0){
            if(appRepository.landExist(land.getData().getId(),land.getData().getSnapshot())){
                action = LandDBAction.UPDATE;
            }
        }
        List<LandZone> zones = appRepository.getLandZonesByLandData(land.getData());

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
        List<LandZoneDataRecord> zoneDataRecords = new ArrayList<>();
        for(LandZone zone:zones){
            if(MapUtil.contains(
                    zone.getData().getBorder(),
                    land.getData().getBorder()
            )){
                List<LatLng> tempBorders = DataUtil.removeSamePointStartEnd(
                        MapUtil.intersection(
                                zone.getData().getBorder(),
                                land.getData().getBorder()
                        )
                );
                for(List<LatLng> hole : land.getData().getHoles()){
                    if(MapUtil.contains(tempBorders,hole)){
                        List<LatLng> tempDifference = MapUtil.getBiggerAreaZoneDifference(tempBorders,hole);
                        tempBorders.clear();
                        tempBorders.addAll(DataUtil.removeSamePointStartEnd(tempDifference));
                    }
                }
                if(tempBorders.size()>0){
                    zone.getData().setBorder(tempBorders);
                    appRepository.saveZone(zone);
                    zoneDataRecords.add(new LandZoneDataRecord(landRecord,zone.getData()));
                }
            }
        }

        appRepository.saveLandRecord(new LandHistoryRecord(landRecord,zoneDataRecords));
        populateLists();
    }
    public void bulkEditLandData(List<LandData> data){
        if(data == null) return;

        for(LandData tempData : data){
            if(tempData == null) continue;

            LandDBAction action = LandDBAction.BULK_EDITED;
            List<LandZone> zones = appRepository.getLandZonesByLandData(tempData);
            appRepository.saveLand(new Land(tempData));
            LandDataRecord landRecord = new LandDataRecord(
                    tempData,
                    action,
                    new Date()
            );
            List<LandZoneDataRecord> zoneDataRecords = new ArrayList<>();
            for(LandZone zone : zones){
                if(zone == null) continue;
                if(zone.getData() == null) continue;

                appRepository.saveZone(zone);
                zoneDataRecords.add(new LandZoneDataRecord(landRecord,zone.getData()));
            }
            appRepository.saveLandRecord(new LandHistoryRecord(landRecord,zoneDataRecords));
        }
        populateLists();
    }
    private boolean removeLandAction(Land land) {
        if(land == null) return false;
        if(land.getData() == null) return false;
        if(appRepository.getLandZonesByLandData(land.getData()).size() > 0) return false;

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                LandDBAction.DELETE,
                new Date()
        );
        appRepository.saveLandRecord(new LandHistoryRecord(landRecord));
        appRepository.deleteLand(land);
        return true;
    }
    public boolean removeLand(Land land) {
        if(land == null) return false;
        if(land.getData() == null) return false;
        if(removeLandAction(land)){
            populateLists();
            return true;
        }
        return false;
    }
    public boolean removeLands(List<Land> lands) {
        if(lands == null) return false;

        boolean ans = true;
        for(Land land:lands){
            if(!removeLandAction(land)) ans = false;
        }
        populateLists();
        return ans;
    }

    public void saveZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;

        Land land = appRepository.getLand( zone.getData().getLid(), zone.getData().getSnapshot() );
        if(land == null) return;

        zone.getData().setSnapshot(land.getData().getSnapshot());

        LandDBAction action = LandDBAction.ZONE_ADDED;
        if(zone.getData().getId() != 0){
            if(appRepository.zoneExist(zone.getData().getId(),zone.getData().getSnapshot())){
                action = LandDBAction.ZONE_UPDATED;
            }
        }

        List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(zone.getData().getBorder());
        zone.getData().setBorder(tempPointList);
        appRepository.saveZone(zone);

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                action,
                new Date()
        );
        List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
        List<LandZone> zones = appRepository.getLandZonesByLandData(land.getData());
        for(LandZone temp : zones){
            zoneRecords.add(new LandZoneDataRecord(landRecord, temp.getData()));
        }
        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));

        populateLists();
    }
    private boolean removeZoneAction(LandZone zone){
        if(zone == null) return false;
        if(zone.getData() == null) return false;

        Land land = appRepository.getLand( zone.getData().getLid(), zone.getData().getSnapshot() );
        if(land == null) return false;

        LandDBAction action = LandDBAction.ZONE_REMOVED;
        appRepository.deleteZone(zone);

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                action,
                new Date()
        );
        List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
        List<LandZone> zones = appRepository.getLandZonesByLandData(land.getData());
        for(LandZone temp : zones){
            zoneRecords.add(new LandZoneDataRecord(landRecord, temp.getData()));
        }
        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));
        return true;
    }
    public boolean removeZone(LandZone zone) {
        if(removeZoneAction(zone)) {
            populateLists();
            return true;
        }
        return false;
    }
    public boolean removeZones(List<LandZone> zones) {
        if(zones == null) return false;
        boolean ans = true;

        for(LandZone zone:zones){
            if(!removeZoneAction(zone)) ans = false;
        }
        if(ans) populateLists();
        return ans;
    }

    public void restoreLand(Land land, List<LandZone> zones) {
        if(land == null) return;
        if(land.getData() == null) return;
        if(land.getData().getId() <= 0) return;

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                LandDBAction.RESTORE,
                new Date()
        );
        List<LandZoneDataRecord> zonesRecord = new ArrayList<>();

        appRepository.saveLand(land);

        if(zones == null) zones = new ArrayList<>();
        List<LandZone> zonesDelete =appRepository.getLandZonesByLandData(land.getData());
        for(LandZone zone : zonesDelete){
            appRepository.deleteZone(zone);
        }
        for(LandZone zone : zones){
            appRepository.saveZone(zone);
            zonesRecord.add(new LandZoneDataRecord(landRecord, zone.getData()));
        }

        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zonesRecord));
        populateLists();
    }

    public void saveNotification(CalendarNotification notification) {
        if(notification == null) return;

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
    }
    public void removeNotification(CalendarNotification notification) {
        if(notification == null) return;

        DataUtil.removeNotificationToAlertManager(
                getApplication().getApplicationContext(),
                notification
        );
        appRepository.deleteNotification(notification);
        populateLists();
    }

    public CalendarCategory getDefaultCategory(){
        return new CalendarCategory(
                AppValues.defaultCalendarCategoryID,
                getApplication().getResources().getString(R.string.calendar_default_category),
                AppValues.defaultCalendarCategoryColor
        );
    }
    public void saveCalendarCategory(CalendarCategory category){
        if(category == null) return;
        appRepository.saveCalendarCategory(category);
        populateLists();
    }
    public boolean removeCalendarCategory(CalendarCategory category){
        if(category != null && !appRepository.calendarCategoryHasNotifications(category.getId())) {
            appRepository.deleteCalendarCategory(category);
            populateLists();
            return true;
        }
        return false;
    }
    public void removeCalendarCategories(List<CalendarCategory> categories) {
        if(categories == null) return;
        for(CalendarCategory category : categories){
            if(category == null) continue;
            if(!appRepository.calendarCategoryHasNotifications(category.getId())) appRepository.deleteCalendarCategory(category);
        }
        populateLists();
    }

    public boolean calendarCategoryHasNotifications(CalendarCategory category) {
        if(category == null) return false;
        return appRepository.calendarCategoryHasNotifications(category.getId());
    }

    private void importLand(Land land){
        if(land == null) return;
        if(land.getData() == null) return;

        LandDBAction action = LandDBAction.IMPORTED;

        List<LandZone> zones = appRepository.getLandZonesByLandData(land.getData());

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
        List<LandZoneDataRecord> zoneDataRecords = new ArrayList<>();

        for(LandZone zone:zones){
            if(MapUtil.contains(
                    zone.getData().getBorder(),
                    land.getData().getBorder()
            )){
                List<LatLng> tempBorders = DataUtil.removeSamePointStartEnd(
                        MapUtil.intersection(
                                zone.getData().getBorder(),
                                land.getData().getBorder()
                        )
                );
                for(List<LatLng> hole : land.getData().getHoles()){
                    if(MapUtil.contains(tempBorders,hole)){
                        List<LatLng> tempDifference = MapUtil.getBiggerAreaZoneDifference(tempBorders,hole);
                        tempBorders.clear();
                        tempBorders.addAll(DataUtil.removeSamePointStartEnd(tempDifference));
                    }
                }
                if(tempBorders.size()>0){
                    zone.getData().setBorder(tempBorders);
                    appRepository.saveZone(zone);
                    zoneDataRecords.add(new LandZoneDataRecord(landRecord,zone.getData()));
                }
            }
        }

        appRepository.saveLandRecord(new LandHistoryRecord(landRecord,zoneDataRecords));
    }
    private void importZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;

        Land land = appRepository.getLand( zone.getData().getLid(), zone.getData().getSnapshot() );
        if(land == null) return;
        zone.getData().setSnapshot(land.getData().getSnapshot());

        LandDBAction action = LandDBAction.ZONE_IMPORTED;

        List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(zone.getData().getBorder());
        zone.getData().setBorder(tempPointList);
        appRepository.saveZone(zone);

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                action,
                new Date()
        );
        List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
        List<LandZone> zones = appRepository.getLandZonesByLandData(land.getData());
        for(LandZone temp : zones){
            zoneRecords.add(new LandZoneDataRecord(landRecord, temp.getData()));
        }
        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));
    }
    public void importLandsAndZones(List<Land> lands, List<LandZone> zones) {
        if(lands != null){
            for(Land land : lands){
                if(land.getData() == null) continue;
                importLand(land);
            }
        }
        if(zones != null){
            for(LandZone zone : zones){
                if(zone.getData() == null) continue;
                importZone(zone);
            }
        }
        populateSnapshotLists();
    }
}
