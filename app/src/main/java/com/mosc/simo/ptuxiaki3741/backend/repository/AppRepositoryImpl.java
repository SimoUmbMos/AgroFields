package com.mosc.simo.ptuxiaki3741.backend.repository;

import com.mosc.simo.ptuxiaki3741.backend.room.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.Snapshot;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AppRepositoryImpl implements AppRepository {
    private final RoomDatabase db;
    private long snapshot;

    public AppRepositoryImpl(RoomDatabase db){
        this.db = db;
        this.snapshot = AppValues.defaultSnapshot;
    }


    @Override
    public void setDefaultSnapshot(long snapshot){
        Snapshot temp = saveSnapshot(snapshot);
        this.snapshot = temp.getKey();
    }

    @Override
    public long getDefaultSnapshot(){
        return snapshot;
    }


    @Override
    public List<Long> getSnapshots(){
        List<Snapshot> snapshots = db.snapshotDao().getDataSnapshots();
        if(snapshots == null) snapshots = new ArrayList<>();
        List<Long> ans = new ArrayList<>();
        for (Snapshot snapshot : snapshots){
            if(snapshot == null) continue;
            List<LandData> lands = db.landDao().getLands(snapshot.getKey());
            if(lands != null && lands.size() > 0) ans.add(snapshot.getKey());
        }
        return ans;
    }

    @Override
    public Snapshot saveSnapshot(long s){
        long snapshot = s;
        if(snapshot <= 0){
            snapshot = this.snapshot;
        }
        if(!db.snapshotDao().snapshotExist(snapshot)){
            db.snapshotDao().insert(new Snapshot(snapshot));
        }
        return db.snapshotDao().getSnapshot(snapshot);
    }


    @Override
    public List<Land> getLands() {
        List<Land> lands = new ArrayList<>();

        List<LandData> landsData = db.landDao().getLands(snapshot);
        if(landsData != null){
            for(LandData landData : landsData){
                lands.add(new Land(landData));
            }
        }
        return lands;
    }

    @Override
    public List<Land> getLands(long snapshot) {
        List<Land> lands = new ArrayList<>();

        List<LandData> landsData = db.landDao().getLands(snapshot);
        if(landsData != null){
            for(LandData landData : landsData){
                lands.add(new Land(landData));
            }
        }

        return lands;
    }

    @Override
    public boolean landExist(long id, long snapshot){
        return db.landDao().landExist(id, snapshot);
    }

    @Override
    public Land getLand(long id, long snapshot) {
        long snap = snapshot;
        if(snap == -1) snap = this.snapshot;
        LandData data = db.landDao().getLand(id, snap);
        if(data != null) return new Land(data);
        return null;
    }

    @Override
    public void saveLand(Land land){
        if(land == null) return;
        if(land.getData() == null) return;
        LandData landData = land.getData();

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(landData.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(landData.getSnapshot());
        }else{
            snapshot = saveSnapshot(landData.getSnapshot());
        }
        landData.setSnapshot(snapshot.getKey());

        if(landData.getId() > 0){
            db.landDao().insert(landData);
            if(landData.getId() >= snapshot.getLandCount()){
                snapshot.setLandCount(landData.getId()+1);
                db.snapshotDao().insert(snapshot);
            }
        }else{
            landData.setId(snapshot.getLandCount());
            snapshot.setLandCount(snapshot.getLandCount()+1);
            db.landDao().insert(landData);
            db.snapshotDao().insert(snapshot);
        }

        land.setData(landData);
    }

    @Override
    public void deleteLand(Land land) {
        if(land.getData() != null)
            db.landDao().delete(land.getData());
    }


    @Override
    public Map<Long,List<LandZone>> getLandZones(){
        Map<Long,List<LandZone>> ans = new HashMap<>();

        List<LandZone> zones;
        List<LandZoneData> zonesData = db.landZoneDao().getZones(snapshot);
        if(zonesData != null){
            for(LandZoneData zoneData : zonesData){
                zones = ans.getOrDefault(zoneData.getLid(),null);
                if(zones == null){
                    zones = new ArrayList<>();
                }
                zones.add(new LandZone(zoneData));
                ans.put(zoneData.getLid(),zones);
            }
        }
        return ans;
    }

    @Override
    public Map<Long,List<LandZone>> getLandZones(long snapshot){
        Map<Long,List<LandZone>> ans = new HashMap<>();

        List<LandZone> zones;
        List<LandZoneData> zonesData = db.landZoneDao().getZones(snapshot);
        if(zonesData != null){
            for(LandZoneData zoneData : zonesData){
                if(zoneData == null) continue;
                zones = ans.getOrDefault(zoneData.getLid(),null);
                if(zones == null){
                    zones = new ArrayList<>();
                    ans.put(zoneData.getLid(),zones);
                }
                zones.add(new LandZone(zoneData));
            }
        }
        return ans;
    }

    @Override
    public boolean zoneExist(long id, long snapshot) {
        return db.landZoneDao().zoneExist(id, snapshot);
    }

    @Override
    public List<LandZone> getLandZonesByLandData(LandData data){
        List<LandZone> ans = new ArrayList<>();
        if(data == null) return ans;
        if(data.getId() <= 0) return ans;

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(data.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(data.getSnapshot());
        }else{
            snapshot = saveSnapshot(data.getSnapshot());
        }

        List<LandZoneData> zones = db.landZoneDao().getLandZonesByLandID(data.getId(), snapshot.getKey());
        if(zones != null){
            for(LandZoneData zone : zones){
                if(zone == null) continue;
                ans.add(new LandZone(zone));
            }
        }
        return ans;
    }

    @Override
    public void saveZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;
        LandZoneData zoneData = zone.getData();

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(zoneData.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(zoneData.getSnapshot());
        }else{
            snapshot = saveSnapshot(zoneData.getSnapshot());
        }
        zoneData.setSnapshot(snapshot.getKey());

        if(zoneData.getId() > 0){
            db.landZoneDao().insert(zoneData);
            if(zoneData.getId() >= snapshot.getZoneCount()){
                snapshot.setZoneCount(zoneData.getId()+1);
                db.snapshotDao().insert(snapshot);
            }
        }else{
            zoneData.setId(snapshot.getZoneCount());
            snapshot.setZoneCount(snapshot.getZoneCount()+1);
            db.landZoneDao().insert(zoneData);
            db.snapshotDao().insert(snapshot);
        }

        zone.setData(zoneData);
    }

    @Override
    public void deleteZone(LandZone zone) {
        if(zone.getData() != null)
            db.landZoneDao().delete(zone.getData());
    }


    @Override
    public List<LandHistoryRecord> getLandRecords() {
        List<LandHistoryRecord> ans = new ArrayList<>();
        List<LandDataRecord> landRecords = db.landHistoryDao().getLandRecords(snapshot);
        for(LandDataRecord landRecord : landRecords){
            ans.add(new LandHistoryRecord(
                    landRecord,
                    db.landZoneHistoryDao().getZoneRecordByRecordIdAndSnapshot(
                            landRecord.getId(),
                            landRecord.getSnapshot()
                    )
            ));
        }
        return ans;
    }

    @Override
    public void saveLandRecord(LandHistoryRecord record) {
        if(record == null) return;
        if(record.getLandData() == null) return;

        LandDataRecord landRecord = record.getLandData();

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(landRecord.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(landRecord.getSnapshot());
        }else{
            snapshot = saveSnapshot(landRecord.getSnapshot());
        }
        landRecord.setSnapshot(snapshot.getKey());

        if(landRecord.getId() > 0){
            db.landHistoryDao().insert(landRecord);
            if(landRecord.getId() >= snapshot.getRecordCount()){
                snapshot.setRecordCount(landRecord.getId()+1);
                db.snapshotDao().insert(snapshot);
            }
        }else{
            landRecord.setId(snapshot.getRecordCount());
            snapshot.setRecordCount(snapshot.getRecordCount()+1);
            db.landHistoryDao().insert(landRecord);
            db.snapshotDao().insert(snapshot);
        }

        for(LandZoneDataRecord zoneRecord : record.getLandZonesData()){
            if(zoneRecord == null) continue;
            if(zoneRecord.getZoneBorder().size() == 0) continue;

            zoneRecord.setRecordID(landRecord.getId());
            zoneRecord.setRecordSnapshot(landRecord.getSnapshot());
            db.landZoneHistoryDao().insert(zoneRecord);
        }
    }


    @Override
    public Map<LocalDate, List<CalendarEntity>> getNotifications() {
        List<CalendarNotification> calendarNotifications = db.calendarNotificationDao().getNotifications();
        if(calendarNotifications == null){
            calendarNotifications = new ArrayList<>();
        }
        List<CalendarCategory> categories = db.calendarCategoriesDao().getCalendarCategories();
        if(categories == null){
            categories = new ArrayList<>();
        }

        List<CalendarEntity> temp;
        CalendarCategory tempCategory;
        LocalDate localDate;

        Map<LocalDate, List<CalendarEntity>> ans = new TreeMap<>();
        for(CalendarNotification calendarNotification : calendarNotifications){
            if(calendarNotification.getDate() == null) continue;

            localDate = DataUtil.dateToLocalDate(calendarNotification.getDate());
            temp = ans.getOrDefault(localDate,null);
            if(temp == null){
                temp = new ArrayList<>();
            }
            tempCategory = null;
            for(CalendarCategory category : categories){
                if(category.getId() == calendarNotification.getCategoryID()){
                    tempCategory = category;
                    break;
                }
            }
            temp.add(new CalendarEntity(tempCategory,calendarNotification));
            ans.put(localDate,temp);
        }

        return ans;
    }

    @Override
    public CalendarNotification getNotification(long id) {
        return db.calendarNotificationDao().getNotificationById(id);
    }

    @Override
    public void saveNotification(CalendarNotification notification) {
        if(notification == null) return;

        if(db.snapshotDao().snapshotExist(notification.getSnapshot())){
            Snapshot snapshot = db.snapshotDao().getSnapshot(notification.getSnapshot());
            notification.setSnapshot(snapshot.getKey());
        }else{
            notification.setSnapshot(snapshot);
        }

        long id = db.calendarNotificationDao().insert(notification);
        notification.setId(id);
    }

    @Override
    public void deleteNotification(CalendarNotification notification) {
        if(notification != null)
            db.calendarNotificationDao().delete(notification);
    }


    @Override
    public List<CalendarCategory> getCalendarCategories() {
        List<CalendarCategory> categories = db.calendarCategoriesDao().getCalendarCategories();
        if(categories == null)
            categories = new ArrayList<>();
        return categories;
    }

    @Override
    public void saveCalendarCategory(CalendarCategory category) {
        if(category == null) return;
        long id = db.calendarCategoriesDao().insert(category);
        category.setId(id);
    }

    @Override
    public void deleteCalendarCategory(CalendarCategory category) {
        if(category != null)
            db.calendarCategoriesDao().delete(category);
    }

    @Override
    public boolean calendarCategoryHasNotifications(long categoryID) {
        List<CalendarNotification> notifications = db.calendarNotificationDao().getNotificationsByCategory(categoryID);
        if(notifications == null) return false;
        return notifications.size() > 0;
    }
}
