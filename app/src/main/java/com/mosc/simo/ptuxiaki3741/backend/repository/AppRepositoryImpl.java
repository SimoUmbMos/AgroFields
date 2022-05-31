package com.mosc.simo.ptuxiaki3741.backend.repository;

import com.mosc.simo.ptuxiaki3741.backend.room.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AppRepositoryImpl implements AppRepository {
    private final RoomDatabase db;
    public AppRepositoryImpl(RoomDatabase db){
        this.db = db;
    }

    @Override
    public List<Land> getAllLands() {
        List<Land> ans = new ArrayList<>();
        List<LandData> temp = db.landDao().getLands();
        if(temp != null)
            temp.forEach(it -> ans.add(new Land(it)));
        return ans;
    }
    @Override
    public List<Land> getLands(long year) {
        List<Land> ans = new ArrayList<>();
        List<LandData> landsData = db.landDao().getLands(year);
        if(landsData != null)
            landsData.forEach(it -> ans.add(new Land(it)));
        return ans;
    }
    @Override
    public List<Long> getLandYears(){
        List<Long> years = db.landDao().getYears();
        if(years != null)
            return years;
        else
            return new ArrayList<>();
    }
    @Override
    public boolean landExist(long id){
        return db.landDao().landExist(id);
    }
    @Override
    public Land getLand(long id) {
        LandData data = db.landDao().getLand(id);
        if(data != null)
            return new Land(data);
        else
            return null;
    }
    @Override
    public void saveLand(Land land){
        if(land == null) return;
        if(land.getData() == null) return;
        long id = db.landDao().insert(land.getData());
        land.getData().setId(id);
    }
    @Override
    public void deleteLand(Land land) {
        if(land == null) return;
        if(land.getData() == null) return;
        db.landDao().delete(land.getData());
    }

    @Override
    public List<LandZone> getAllLandZones() {
        List<LandZone> ans = new ArrayList<>();
        List<LandZoneData> temp = db.landZoneDao().getZones();
        if(temp != null)
            temp.forEach(it -> ans.add(new LandZone(it)));
        return ans;
    }
    @Override
    public Map<Long,List<LandZone>> getLandZones(long year){
        Map<Long,List<LandZone>> ans = new HashMap<>();
        List<LandZoneData> zonesData = db.landZoneDao().getZones(year);
        if(zonesData != null){
            zonesData.forEach(it->{
                List<LandZone> zones = ans.getOrDefault(it.getLid(),null);
                if(zones == null) zones = new ArrayList<>();
                zones.add(new LandZone(it));
                ans.put(it.getLid(),zones);
            });
        }
        return ans;
    }
    @Override
    public LandZone getLandZone(Long id) {
        LandZoneData data = db.landZoneDao().getLandZone(id);
        if(data != null)
            return new LandZone(data);
        else
            return null;
    }
    @Override
    public boolean zoneExist(long id) {
        return db.landZoneDao().zoneExist(id);
    }
    @Override
    public List<LandZone> getLandZonesByLandID(long id){
        List<LandZone> ans = new ArrayList<>();
        if(id <= 0) return ans;
        List<LandZoneData> zones = db.landZoneDao().getLandZonesByLandID(id);
        if(zones != null)
            zones.forEach(it-> ans.add(new LandZone(it)));
        return ans;
    }
    @Override
    public void saveZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;
        long id = db.landZoneDao().insert(zone.getData());
        zone.getData().setId(id);
    }
    @Override
    public void deleteZone(LandZone zone) {
        if(zone == null) return;
        if(zone.getData() == null) return;
        db.landZoneDao().delete(zone.getData());
    }

    @Override
    public List<LandHistoryRecord> getLandRecords(long year) {
        List<LandHistoryRecord> ans = new ArrayList<>();
        List<LandDataRecord> landRecords = db.landHistoryDao().getLandRecords(year);
        if(landRecords == null) return ans;
        landRecords.forEach(it->{
            List<LandZoneDataRecord> temp = db.landZoneHistoryDao()
                    .getZoneRecordByRecordId(it.getId());
            if(temp != null)
                ans.add(new LandHistoryRecord(it, temp));
            else
                ans.add(new LandHistoryRecord(it, new ArrayList<>()));
        });
        return ans;
    }
    @Override
    public void saveLandRecord(LandHistoryRecord record) {
        if(record == null) return;
        if(record.getLandData() == null) return;
        long rid = db.landHistoryDao().insert(record.getLandData());
        record.getLandData().setId(rid);
        record.getLandZonesData().forEach(it->{
            it.setRecordID(rid);
            long id = db.landZoneHistoryDao().insert(it);
            it.setId(id);
        });
    }

    @Override
    public List<CalendarNotification> getAllNotifications() {
        List<CalendarNotification> ans = db.calendarNotificationDao().getNotifications();
        if(ans != null)
            return ans;
        else
            return new ArrayList<>();
    }
    @Override
    public Map<LocalDate, List<CalendarEntity>> getNotifications() {
        Map<LocalDate, List<CalendarEntity>> ans = new TreeMap<>();

        List<CalendarNotification> calendarNotifications = db.calendarNotificationDao().getNotifications();
        if(calendarNotifications == null) return ans;

        List<CalendarCategory> categories = getCalendarCategories();
        calendarNotifications.forEach(it->{
            LocalDate localDate = DataUtil.dateToLocalDate(it.getDate());
            List<CalendarEntity> temp = ans.getOrDefault(localDate,null);
            if(temp == null) temp = new ArrayList<>();
            CalendarCategory tempCategory = null;
            for(CalendarCategory category : categories){
                if(it.getCategoryID() == category.getId()){
                    tempCategory = category;
                    break;
                }
            }
            temp.add(new CalendarEntity(tempCategory, it));
            ans.put(localDate, temp);
        });

        return ans;
    }
    @Override
    public CalendarNotification getNotification(long id) {
        return db.calendarNotificationDao().getNotificationById(id);
    }
    @Override
    public List<CalendarNotification> getAllNotificationsByLid(long lid) {
        List<CalendarNotification> ans = db.calendarNotificationDao()
                .getNotificationByLid(lid);
        if(ans != null)
            return ans;
        else
            return new ArrayList<>();
    }
    @Override
    public List<CalendarNotification> getAllNotificationsByZid(long zid) {
        List<CalendarNotification> ans = db.calendarNotificationDao()
                .getAllNotificationsByZid(zid);
        if(ans != null)
            return ans;
        else
            return new ArrayList<>();
    }
    @Override
    public void saveNotification(CalendarNotification notification) {
        if(notification == null) return;
        long id = db.calendarNotificationDao().insert(notification);
        notification.setId(id);
    }
    @Override
    public void deleteNotification(CalendarNotification notification) {
        if(notification == null) return;
        db.calendarNotificationDao().delete(notification);
    }

    @Override
    public List<CalendarCategory> getCalendarCategories() {
        List<CalendarCategory> ans = db.calendarCategoriesDao().getCalendarCategories();
        if(ans != null)
            return ans;
        else
            return new ArrayList<>();
    }
    @Override
    public CalendarCategory getCalendarCategory(long id) {
        return db.calendarCategoriesDao().getCalendarCategory(id);
    }
    @Override
    public boolean calendarCategoryHasNotifications(long categoryID) {
        List<CalendarNotification> notifications = db.calendarNotificationDao().getNotificationsByCategory(categoryID);
        if(notifications == null) return false;
        return notifications.size() > 0;
    }
    @Override
    public void saveCalendarCategory(CalendarCategory category) {
        if(category == null) return;
        long id = db.calendarCategoriesDao().insert(category);
        category.setId(id);
    }
    @Override
    public void deleteCalendarCategory(CalendarCategory category) {
        if(category == null) return;
        db.calendarCategoriesDao().delete(category);
    }
}
