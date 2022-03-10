package com.mosc.simo.ptuxiaki3741.backend.repository;

import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.entities.Snapshot;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AppRepositoryImpl implements AppRepository {
    private final RoomDatabase db;
    private Snapshot snapshot;

    public AppRepositoryImpl(RoomDatabase db){
        this.db = db;
        this.snapshot = Snapshot.getInstance();
    }

    @Override
    public void setDefaultSnapshot(Snapshot snapshot){
        this.snapshot = saveSnapshot(snapshot);
    }

    @Override
    public Snapshot getDefaultSnapshot(){
        return snapshot;
    }

    @Override
    public List<Snapshot> getSnapshots(){
        List<Snapshot> ans = db.snapshotDao().getDataSnapshots();
        if(ans == null) ans = new ArrayList<>();
        return ans;
    }

    @Override
    public List<String> getLandTags(){
        List<String> ans = new ArrayList<>();

        List<LandData> landsData = db.landDao().getLands(snapshot.getKey());
        if(landsData == null) return ans;

        for(LandData landData : landsData){
            List<String> tags = LandUtil.getLandTags(landData);
            for(String tag : tags){
                if(!ans.contains(tag)) {
                    ans.add(tag);
                }
            }
        }

        return ans;
    }

    @Override
    public Land getLand(long id, long snapshot) {
        LandData data = db.landDao().getLand(id, snapshot);
        if(data != null) return new Land(data);
        return null;
    }

    @Override
    public List<Land> getLands() {
        List<Land> lands = new ArrayList<>();

        List<LandData> landsData = db.landDao().getLands(snapshot.getKey());
        if(landsData != null){
            for(LandData landData : landsData){
                lands.add(new Land(landData));
            }
        }
        return lands;
    }

    @Override
    public Map<Long,List<LandZone>> getLandZones(){
        Map<Long,List<LandZone>> ans = new HashMap<>();

        List<LandZone> zones;
        List<LandZoneData> zonesData = db.landZoneDao().getZones(snapshot.getKey());
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
    public List<LandHistoryRecord> getLandRecords() {
        List<LandHistoryRecord> ans = new ArrayList<>();
        List<LandDataRecord> landRecords = db.landHistoryDao().getLandRecords(snapshot.getKey());
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
    public Map<LocalDate, List<CalendarNotification>> getNotifications() {
        Map<LocalDate, List<CalendarNotification>> ans = new TreeMap<>();
        List<CalendarNotification> calendarNotifications =
                db.calendarNotificationDao().getNotifications(snapshot.getKey());

        List<CalendarNotification> temp;
        LocalDate localDate;
        if(calendarNotifications != null){
            for(CalendarNotification calendarNotification : calendarNotifications){
                if(calendarNotification.getDate() == null) continue;

                localDate = DataUtil.dateToLocalDate(calendarNotification.getDate());
                temp = ans.getOrDefault(localDate,null);
                if(temp == null){
                    temp = new ArrayList<>();
                }
                temp.add(calendarNotification);
                ans.put(localDate,temp);
            }
        }

        return ans;
    }

    @Override
    public boolean landExist(long id, long snapshot){
        return db.landDao().landExist(id, snapshot);
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
            snapshot = saveSnapshot(new Snapshot(data.getSnapshot()));
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
    public CalendarNotification getNotification(long id, long snapshot) {
        return db.calendarNotificationDao().getNotificationById(id, snapshot);
    }

    @Override
    public Snapshot saveSnapshot(Snapshot snapshot){
        if(snapshot.getKey() <= 0){
            snapshot.setKey(this.snapshot.getKey());
        }
        if(snapshot.needsInit()){
            if(db.snapshotDao().snapshotExist(snapshot.getKey())){
                snapshot = db.snapshotDao().getSnapshot(snapshot.getKey());
            }else{
                snapshot.setInit(false);
                db.snapshotDao().insert(snapshot);
            }
        }else{
            if(!db.snapshotDao().snapshotExist(snapshot.getKey())){
                snapshot.setInit(false);
                db.snapshotDao().insert(snapshot);
            }
        }
        return snapshot;
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
            snapshot = saveSnapshot(new Snapshot(landData.getSnapshot()));
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
    public void saveZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;
        LandZoneData zoneData = zone.getData();

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(zoneData.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(zoneData.getSnapshot());
        }else{
            snapshot = saveSnapshot(new Snapshot(zoneData.getSnapshot()));
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
    public void saveLandRecord(LandHistoryRecord record) {
        if(record == null) return;
        if(record.getLandData() == null) return;

        LandDataRecord landRecord = record.getLandData();

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(landRecord.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(landRecord.getSnapshot());
        }else{
            snapshot = saveSnapshot(new Snapshot(landRecord.getSnapshot()));
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
    public void saveNotification(CalendarNotification notification) {
        if(notification == null) return;

        Snapshot snapshot;
        if(db.snapshotDao().snapshotExist(notification.getSnapshot())){
            snapshot = db.snapshotDao().getSnapshot(notification.getSnapshot());
        }else{
            snapshot = saveSnapshot(new Snapshot(notification.getSnapshot()));
        }
        notification.setSnapshot(snapshot.getKey());

        if(notification.getId() > 0){
            db.calendarNotificationDao().insert(notification);
            if(notification.getId() >= snapshot.getCalendarCount()){
                snapshot.setCalendarCount(notification.getId()+1);
                db.snapshotDao().insert(snapshot);
            }
        }else{
            notification.setId(snapshot.getCalendarCount());
            snapshot.setCalendarCount(snapshot.getCalendarCount()+1);
            db.calendarNotificationDao().insert(notification);
            db.snapshotDao().insert(snapshot);
        }

    }

    @Override
    public void deleteLand(Land land) {
        if(land.getData() != null)
            db.landDao().delete(land.getData());
    }

    @Override
    public void deleteZone(LandZone zone) {
        if(zone.getData() != null)
            db.landZoneDao().delete(zone.getData());
    }

    @Override
    public void deleteNotification(CalendarNotification notification) {
        if(notification != null)
            db.calendarNotificationDao().delete(notification);
    }
}
