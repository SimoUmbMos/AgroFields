package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.file.openxml.OpenXmlState;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
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
    private long defaultYear;

    private final MutableLiveData<List<Long>> snapshots = new MutableLiveData<>();
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<Land>> snapshotLands = new MutableLiveData<>();
    private final MutableLiveData<Map<Long,List<LandZone>>> landZones = new MutableLiveData<>();
    private final MutableLiveData<Map<Long,List<LandZone>>> snapshotLandZones = new MutableLiveData<>();
    private final MutableLiveData<List<LandHistoryRecord>> landsHistory = new MutableLiveData<>();

    private final MutableLiveData<List<CalendarCategory>> calendarCategories = new MutableLiveData<>();
    private final MutableLiveData<Map<LocalDate,List<CalendarEntity>>> notifications = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainApplication.getRoomDb(application);
        appRepository = new AppRepositoryImpl(db);
        defaultYear = LocalDate.now().getYear();
    }

    public LiveData<List<Long>> getSnapshots(){
        return snapshots;
    }
    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public LiveData<List<Land>> getTempSnapshotLands(){
        return snapshotLands;
    }
    public LiveData<Map<Long,List<LandZone>>> getLandZones(){
        return landZones;
    }
    public LiveData<Map<Long,List<LandZone>>> getTempSnapshotLandZones(){
        return snapshotLandZones;
    }
    public LiveData<List<LandHistoryRecord>> getLandsHistory() {
        return landsHistory;
    }

    public LiveData<List<CalendarCategory>> getCalendarCategories(){return calendarCategories;}
    public LiveData<Map<LocalDate,List<CalendarEntity>>> getNotifications() {
        return notifications;
    }

    public void setDefaultSnapshot(long year){
        defaultYear = year;
        populateLists();
    }
    public long getDefaultSnapshot(){
        return defaultYear;
    }

    private void populateLists() {
        populateDataSnapshots();
        populateLands();
        populateLandZones();
        populateLandsRecords();
        populateCalendarCategories();
        populateNotifications();
    }
    private void populateSnapshotLists(){
        populateDataSnapshots();
        populateLands();
        populateLandZones();
        populateLandsRecords();
    }
    private void populateDataSnapshots() {
        List<Long> snapshotsList = appRepository.getLandYears();
        if(snapshotsList == null)
            snapshotsList = new ArrayList<>();
        snapshots.postValue(snapshotsList);
    }
    private void populateLands() {
        List<Land> landList = appRepository.getLands(getDefaultSnapshot());
        if(landList == null)
            landList = new ArrayList<>();
        lands.postValue(landList);
    }
    private void populateLandZones(){
        Map<Long,List<LandZone>> zoneList = appRepository.getLandZones(getDefaultSnapshot());
        if(zoneList == null)
            zoneList = new HashMap<>();
        landZones.postValue(zoneList);
    }
    private void populateLandsRecords() {
        List<LandHistoryRecord> landsHistoryList = appRepository.getLandRecords(getDefaultSnapshot());
        if(landsHistoryList == null)
            landsHistoryList = new ArrayList<>();
        landsHistory.postValue(landsHistoryList);
    }
    private void populateCalendarCategories() {
        List<CalendarCategory> categories = appRepository.getCalendarCategories();
        if(categories == null) categories = new ArrayList<>();
        categories.add(0,getDefaultCategory());
        calendarCategories.postValue(categories);
    }
    private void populateNotifications() {
        Map<LocalDate,List<CalendarEntity>> notificationsList = appRepository.getNotifications();
        if(notificationsList == null) notificationsList = new TreeMap<>();
        CalendarCategory defaultCategory = getDefaultCategory();
        notificationsList.forEach((date,notifications) -> notifications.forEach(notification -> {
            if(notification.getCategory() == null){
                notification.setCategory(defaultCategory);
                notification.getNotification().setCategoryID(defaultCategory.getId());
            }
        }));
        notifications.postValue(notificationsList);
    }

    public void saveLand(Land land){
        if(land == null) return;
        if(land.getData() == null) return;

        LandDBAction action = LandDBAction.CREATE;
        if(land.getData().getId() != 0 && appRepository.landExist(land.getData().getId())){
            action = LandDBAction.UPDATE;
        }
        List<LandZone> zones = appRepository.getLandZonesByLandID(land.getData().getId());
        List<CalendarNotification> notifications = appRepository.getAllNotificationsByLid(land.getData().getId());

        List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(land.getData().getBorder());
        List<List<LatLng>> tempHoles = new ArrayList<>();
        land.getData().getHoles().forEach(it-> tempHoles.add(DataUtil.removeSamePointStartEnd(it)));
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
        notifications.forEach(notification -> {
            removeNotificationAction(notification);
            saveNotificationAction(notification);
        });
        populateLists();
    }
    public void bulkEditLandData(List<LandData> data){
        if(data == null) return;
        data.forEach(it->{
            List<LandZone> zones = appRepository.getLandZonesByLandID(it.getId());
            List<CalendarNotification> notifications = appRepository.getAllNotificationsByLid(it.getId());

            appRepository.saveLand(new Land(it));
            LandDataRecord landRecord = new LandDataRecord(
                    it,
                    LandDBAction.BULK_EDITED,
                    new Date()
            );
            List<LandZoneDataRecord> zoneDataRecords = new ArrayList<>();
            zones.forEach(zone->{
                appRepository.saveZone(zone);
                zoneDataRecords.add(new LandZoneDataRecord(landRecord,zone.getData()));
            });
            appRepository.saveLandRecord(new LandHistoryRecord(landRecord,zoneDataRecords));
            notifications.forEach(notification -> {
                removeNotificationAction(notification);
                saveNotificationAction(notification);
            });
        });
        populateLists();
    }
    private boolean removeLandAction(Land land) {
        if(land == null) return false;
        if(land.getData() == null) return false;
        if(appRepository.getLandZonesByLandID(
                land.getData().getId()
        ).size() > 0) return false;
        if(appRepository.getAllNotificationsByLid(
                land.getData().getId()
        ).size() > 0) return false;

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

        boolean needUpdate = false;
        for(Land land:lands){
            if(removeLandAction(land)) {
                needUpdate = true;
            }else{
                ans = false;
            }
        }
        if(needUpdate)
            populateLists();

        return ans;
    }

    public void saveZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;

        Land land = appRepository.getLand(zone.getData().getLid());
        if(land == null || land.getData() == null) return;

        LandDBAction action = LandDBAction.ZONE_ADDED;
        if(zone.getData().getId() != 0 && appRepository.zoneExist(zone.getData().getId())){
            action = LandDBAction.ZONE_UPDATED;
        }

        List<CalendarNotification> notifications = appRepository.getAllNotificationsByZid(
                zone.getData().getId()
        );

        List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(zone.getData().getBorder());
        if(tempPointList.size() < 3) return;
        zone.getData().setBorder(tempPointList);
        appRepository.saveZone(zone);

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                action,
                new Date()
        );
        List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
        List<LandZone> zones = appRepository.getLandZonesByLandID(land.getData().getId());
        zones.forEach(it -> zoneRecords.add(new LandZoneDataRecord(landRecord, it.getData())));
        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));
        notifications.forEach(notification -> {
            removeNotificationAction(notification);
            saveNotificationAction(notification);
        });
        populateLists();
    }
    public void bulkEditZoneData(List<LandZoneData> data){
        if(data == null) return;
        data.forEach(it->{
            Land land = appRepository.getLand(it.getLid());
            if(land == null) return;
            List<CalendarNotification> notifications = appRepository.getAllNotificationsByZid(
                    it.getId()
            );
            appRepository.saveZone(new LandZone(it));
            LandDBAction action = LandDBAction.ZONE_UPDATED;
            LandDataRecord landRecord = new LandDataRecord(
                    land.getData(),
                    action,
                    new Date()
            );
            List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
            List<LandZone> zones = appRepository.getLandZonesByLandID(land.getData().getId());
            zones.forEach(zone-> zoneRecords.add(new LandZoneDataRecord(landRecord, zone.getData())));
            appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));
            notifications.forEach(notification -> {
                removeNotificationAction(notification);
                saveNotificationAction(notification);
            });
        });
        populateLists();
    }
    private boolean removeZoneAction(LandZone zone){
        if(zone == null) return false;
        if(zone.getData() == null) return false;
        if(appRepository.getAllNotificationsByZid(
                zone.getData().getId()
        ).size() > 0) return false;

        Land land = appRepository.getLand(zone.getData().getLid());
        appRepository.deleteZone(zone);
        if(land != null) {
            LandDataRecord landRecord = new LandDataRecord(
                    land.getData(),
                    LandDBAction.ZONE_REMOVED,
                    new Date()
            );
            List<LandZone> zones = appRepository.getLandZonesByLandID(land.getData().getId());
            List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
            zones.forEach(it-> zoneRecords.add(new LandZoneDataRecord(landRecord, it.getData())));
            appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));
        }
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

        boolean needUpdate = false;
        for(LandZone zone:zones){
            if(removeZoneAction(zone)) {
                needUpdate = true;
            }else{
                ans = false;
            }
        }
        if(needUpdate)
            populateLists();

        return ans;
    }

    public void restoreLand(Land land, List<LandZone> zones) {
        if(land == null) return;
        if(land.getData() == null) return;
        if(land.getData().getId() == 0) return;

        List<CalendarNotification> notifications = appRepository.getAllNotificationsByLid(land.getData().getId());

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                LandDBAction.RESTORE,
                new Date()
        );
        List<LandZoneDataRecord> zonesRecord = new ArrayList<>();

        appRepository.saveLand(land);

        if(zones == null) zones = new ArrayList<>();
        appRepository.getLandZonesByLandID(land.getData().getId())
                .forEach(appRepository::deleteZone);
        zones.forEach(it->{
            appRepository.saveZone(it);
            zonesRecord.add(new LandZoneDataRecord(landRecord, it.getData()));
        });

        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zonesRecord));
        notifications.forEach(notification -> {
            removeNotificationAction(notification);
            saveNotificationAction(notification);
        });
        populateLists();
    }

    private boolean saveNotificationAction(CalendarNotification notification) {
        if(notification == null) return false;
        if(notification.getLid() != null && !appRepository.landExist(notification.getLid())){
            return false;
        }
        if(notification.getZid() != null && !appRepository.zoneExist(notification.getZid())){
            return false;
        }
        if(notification.getId() != 0){
            DataUtil.removeNotificationToAlertManager(
                    getApplication().getApplicationContext(),
                    appRepository.getNotification(notification.getId())
            );
        }
        appRepository.saveNotification(notification);
        return true;
    }
    private boolean removeNotificationAction(CalendarNotification notification) {
        if(notification == null) return false;
        DataUtil.removeNotificationToAlertManager(
                getApplication().getApplicationContext(),
                notification
        );
        appRepository.deleteNotification(notification);
        return true;
    }
    public void saveNotification(CalendarNotification notification) {
        if(saveNotificationAction(notification)){
            populateLists();
            DataUtil.addNotificationToAlertManager(
                    getApplication().getApplicationContext(),
                    notification
            );
        }
    }
    public void removeNotification(CalendarNotification notification) {
        if(removeNotificationAction(notification))
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
        if(category.getId() == 0){
            List<CalendarCategory> categories = appRepository.getCalendarCategories();
            for(CalendarCategory temp :categories){
                if(temp == null) continue;
                if(temp.getName().equals(category.getName())){
                    category.setId(temp.getId());
                    break;
                }
            }
        }
        appRepository.saveCalendarCategory(category);
        populateLists();
    }
    public boolean removeCalendarCategory(CalendarCategory category){
        if(category == null)
            return false;
        if(!appRepository.calendarCategoryHasNotifications(category.getId())) {
            appRepository.deleteCalendarCategory(category);
            populateLists();
            return true;
        }else
            return false;
    }
    public void removeCalendarCategories(List<CalendarCategory> categories) {
        if(categories == null) return;
        for(CalendarCategory category : categories){
            if(category == null) continue;
            if(!appRepository.calendarCategoryHasNotifications(category.getId()))
                appRepository.deleteCalendarCategory(category);
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
        if(land.getData().getBorder() == null) return;

        if(land.getData().getYear() < 1){
            land.getData().setYear(getDefaultSnapshot());
        }
        String landTitle = DataUtil.removeSpecialCharacters(land.getData().getTitle());
        if(landTitle.isEmpty()) return;
        land.getData().setTitle(landTitle);

        List<String> tags = LandUtil.getLandTags(land.getData());
        land.getData().setTags(LandUtil.getTagsString(tags));

        List<LatLng> tempPointList = DataUtil.removeSamePointStartEnd(land.getData().getBorder());
        if(tempPointList.size() < 3) return;
        land.getData().setBorder(tempPointList);

        List<List<LatLng>> holes = land.getData().getHoles();
        if(holes == null) holes = new ArrayList<>();
        List<List<LatLng>> tempHoles = new ArrayList<>();
        for(List<LatLng> hole : holes){
            List<LatLng> tempHole = DataUtil.removeSamePointStartEnd(hole);
            if(tempHole.size() > 2) tempHoles.add(tempHole);
        }
        land.getData().setHoles(tempHoles);

        List<LandZone> zones = appRepository.getLandZonesByLandID(land.getData().getId());
        appRepository.saveLand(land);
        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                LandDBAction.IMPORTED,
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

        Land land = appRepository.getLand( zone.getData().getLid() );
        if(land == null || land.getData() == null) return;

        String zoneTitle = DataUtil.removeSpecialCharacters(zone.getData().getTitle());
        if(zoneTitle.isEmpty()) return;
        zone.getData().setTitle(zoneTitle);

        String zoneNote = zone.getData().getNote().replaceAll("\n+", " ").trim().replaceAll(" +", " ");
        if(zoneNote.length() > 100) zoneNote = zoneNote.substring(0,99);
        zone.getData().setNote(zoneNote);

        List<String> tags = LandUtil.getLandZoneTags(zone.getData());
        zone.getData().setTags(LandUtil.getTagsString(tags));

        List<LatLng> tempBorder = DataUtil.formatZonePoints(
                zone.getData().getBorder(),
                land,
                appRepository.getLandZonesByLandID(
                        land.getData().getId()
                ));
        if( tempBorder.size() == 0) return;
        zone.getData().setBorder(tempBorder);

        appRepository.saveZone(zone);

        LandDataRecord landRecord = new LandDataRecord(
                land.getData(),
                LandDBAction.ZONE_IMPORTED,
                new Date()
        );
        List<LandZoneDataRecord> zoneRecords = new ArrayList<>();
        List<LandZone> zones = appRepository.getLandZonesByLandID(
                land.getData().getId()
        );
        for(LandZone temp : zones){
            zoneRecords.add(new LandZoneDataRecord(landRecord, temp.getData()));
        }
        appRepository.saveLandRecord(new LandHistoryRecord(landRecord, zoneRecords));
    }
    private void importCategories(CalendarCategory category) {
        if(category == null) return;
        if(category.getId() == 0){
            List<CalendarCategory> categories = appRepository.getCalendarCategories();
            for(CalendarCategory temp :categories){
                if(temp == null) continue;
                if(temp.getName().equals(category.getName())){
                    category.setId(temp.getId());
                    break;
                }
            }
        }
        appRepository.saveCalendarCategory(category);
    }
    private void importNotification(CalendarNotification notification) {
        if(notification == null) return;

        if(
                appRepository.getCalendarCategory(notification.getCategoryID()) != null ||
                notification.getCategoryID() == AppValues.defaultCalendarCategoryID
        ){
            if(notification.getYear() < 1){
                notification.setYear(getDefaultSnapshot());
            }
            if(notification.getLid() != null){
                Land land = appRepository.getLand(notification.getLid());
                if(land == null || land.getData() == null) return;

                if(notification.getZid() != null){
                    LandZone zone = appRepository.getLandZone(notification.getZid());
                    if(zone == null || zone.getData() == null) return;
                }
            }
            if(notification.getId() != 0){
                DataUtil.removeNotificationToAlertManager(
                        getApplication().getApplicationContext(),
                        appRepository.getNotification(notification.getId())
                );
            }
            appRepository.saveNotification(notification);
            DataUtil.addNotificationToAlertManager(
                    getApplication().getApplicationContext(),
                    notification
            );
        }
    }
    public void importFromOpenXmlFile(OpenXmlState state) {
        state.getLands().forEach(this::importLand);
        state.getZones().forEach(this::importZone);
        state.getCategories().forEach(this::importCategories);
        state.getNotifications().forEach(this::importNotification);
        populateLists();
    }
    public void importFromSnapshotToAnotherSnapshot(long fromYear, long toYear){
        long from = fromYear;
        long to = toYear;

        from = Math.max(from, AppValues.minSnapshot);
        to = Math.max(to, AppValues.minSnapshot);

        from = Math.min(from, AppValues.maxSnapshot);
        to = Math.min(to, AppValues.maxSnapshot);

        if(from == to) return;

        List<Land> lands = appRepository.getLands(from);
        if(lands == null) lands = new ArrayList<>();

        for(Land land : lands){
            if(land == null || land.getData() == null) continue;

            List<LandZone> zones = appRepository.getLandZonesByLandID(
                    land.getData().getId()
            );

            land.getData().setId(0);
            land.getData().setYear(to);
            appRepository.saveLand(land);
            LandDataRecord landRecord = new LandDataRecord(
                    land.getData(),
                    LandDBAction.CREATE,
                    new Date()
            );

            List<LandZoneDataRecord> zoneDataRecords = new ArrayList<>();
            if(zones == null) zones = new ArrayList<>();

            for(LandZone landZone : zones){
                if(landZone == null || landZone.getData() == null) continue;

                landZone.getData().setId(0);
                landZone.getData().setLid(land.getData().getId());
                appRepository.saveZone(landZone);
                zoneDataRecords.add(new LandZoneDataRecord(landRecord,landZone.getData()));
            }

            appRepository.saveLandRecord(new LandHistoryRecord(landRecord,zoneDataRecords));
        }
        populateSnapshotLists();
    }

    public long setTempSnapshot(long year){
        if(year < 0){
            snapshotLands.postValue(appRepository.getLands(getDefaultSnapshot()));
            snapshotLandZones.postValue(appRepository.getLandZones(getDefaultSnapshot()));
            return getDefaultSnapshot();
        }else{
            snapshotLands.postValue(appRepository.getLands(year));
            snapshotLandZones.postValue(appRepository.getLandZones(year));
            return year;
        }
    }

    public List<Land> getLands(long year){
        return appRepository.getLands(year);
    }

    public List<Land> getAllLands() {
        return appRepository.getAllLands();
    }
    public List<LandZone> getAllLandZones() {
        return appRepository.getAllLandZones();
    }
    public List<CalendarNotification> getAllNotifications() {
        return appRepository.getAllNotifications();
    }
    public List<CalendarCategory> getAllCalendarCategories() {
        return appRepository.getCalendarCategories();
    }
}
