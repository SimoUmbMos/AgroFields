package com.mosc.simo.ptuxiaki3741.data.util;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.data.models.LandHistoryRecord;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneDataRecord;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.util.ArrayList;
import java.util.List;

public final class LandUtil {
    private LandUtil(){}

    public static List<String> getLandTags(LandData landData){
        if(landData == null) return new ArrayList<>();
        return DataUtil.splitTags(landData.getTags());
    }

    public static List<String> getLandsTags(List<Land> lands){
        if(lands == null) return new ArrayList<>();
        List<String> ans = new ArrayList<>();
        for(Land land : lands){
            if(land == null || land.getData() == null) continue;
            List<String> landTags = getLandTags(land.getData());
            for(String landTag : landTags){
                if(!ans.contains(landTag)) ans.add(landTag);
            }
        }
        return ans;
    }

    public static List<String> getLandZoneTags(LandZoneData zoneData){
        if(zoneData == null) return new ArrayList<>();
        return DataUtil.splitTags(zoneData.getTags());
    }

    public static List<String> getLandZonesTags(List<LandZone> zones){
        if(zones == null) return new ArrayList<>();
        List<String> ans = new ArrayList<>();
        for(LandZone zone : zones){
            if(zone == null || zone.getData() == null) continue;
            List<String> landTags = getLandZoneTags(zone.getData());
            for(String landTag : landTags){
                if(!ans.contains(landTag)) ans.add(landTag);
            }
        }
        return ans;
    }

    public static LandData uniteLandData(LandData currLandData, LandData landDataToAdd){
        List<LatLng> newBorder = new ArrayList<>(currLandData.getBorder());
        List<List<LatLng>> holes = new ArrayList<>(currLandData.getHoles());

        if(!MapUtil.disjoint(currLandData.getBorder(),landDataToAdd.getBorder())){
            List<List<LatLng>> temp;
            newBorder.clear();
            holes.clear();

            newBorder = MapUtil.union(currLandData.getBorder(),landDataToAdd.getBorder());
            for(List<LatLng> hole:currLandData.getHoles()){
                if(MapUtil.disjoint(hole, landDataToAdd.getBorder())){
                    holes.add(hole);
                }else{
                    temp = MapUtil.difference(hole,landDataToAdd.getBorder());
                    if(temp.size()>0) {
                        for (List<LatLng> tempHole : temp) {
                            if (tempHole.size() > 0)
                                holes.add(tempHole);
                        }
                    }
                }
            }
            for(List<LatLng> hole:landDataToAdd.getHoles()){
                if(MapUtil.disjoint(hole, currLandData.getBorder())){
                    holes.add(hole);
                }else{
                    temp = MapUtil.difference(hole,currLandData.getBorder());
                    if(temp.size()>0) {
                        for (List<LatLng> tempHole : temp) {
                            if (tempHole.size() > 0)
                                holes.add(tempHole);
                        }
                    }
                }
            }
        }

        return new LandData(
                currLandData.getId(),
                currLandData.getSnapshot(),
                currLandData.getTitle(),
                currLandData.getTags(),
                currLandData.getColor(),
                newBorder,
                holes
        );
    }
    public static LandData subtractLandData(LandData currLandData, LandData landDataToSubtract){
        List<LatLng> newBorder = new ArrayList<>(currLandData.getBorder());
        List<List<LatLng>> holes = new ArrayList<>(currLandData.getHoles());

        if(!MapUtil.disjoint(currLandData.getBorder(),landDataToSubtract.getBorder())){
            List<LatLng> newHole = new ArrayList<>(landDataToSubtract.getBorder());
            newBorder.clear();
            holes.clear();

            for(List<LatLng> hole:currLandData.getHoles()){
                if(MapUtil.disjoint(hole, newHole)){
                    holes.add(hole);
                }else{
                    newHole = MapUtil.union(hole,newHole);
                }
            }
            List<List<LatLng>> ans = MapUtil.difference(
                    currLandData.getBorder(),
                    newHole
            );
            if(ans.size() > 0){
                newBorder.addAll(ans.get(0));
                if(newBorder.size()>0){
                    double area1 = MapUtil.area(currLandData.getBorder());
                    double area3 = MapUtil.area(newBorder);
                    if(area1 == area3) {
                        holes.add(newHole);
                        newBorder = currLandData.getBorder();
                    }
                }else{
                    holes = new ArrayList<>();
                }
            }else{
                holes = new ArrayList<>();
            }
        }

        return new LandData(
                currLandData.getId(),
                currLandData.getSnapshot(),
                currLandData.getTitle(),
                currLandData.getTags(),
                currLandData.getColor(),
                newBorder,
                holes
        );
    }

    public static LandData getLandDataFromLandRecord(LandHistoryRecord r){
        if(r != null){
            LandDataRecord record = r.getLandData();
            return new LandData(
                    record.getLandID(),
                    record.getSnapshot(),
                    record.getLandTitle(),
                    record.getLandTags(),
                    record.getLandColor(),
                    record.getBorder(),
                    record.getHoles()
            );
        }
        return null;
    }

    public static ArrayList<LandZone> getLandZonesFromLandRecord(LandHistoryRecord r){
        ArrayList<LandZone> zones = new ArrayList<>();
        if(r == null ) return zones;
        List<LandZoneDataRecord> zoneRecords = r.getLandZonesData();
        for(LandZoneDataRecord zoneRecord : zoneRecords){
            LandZoneData data = new LandZoneData(
                    zoneRecord.getZoneID(),
                    zoneRecord.getRecordSnapshot(),
                    r.getLandData().getLandID(),
                    zoneRecord.getZoneTitle(),
                    zoneRecord.getZoneNote(),
                    zoneRecord.getZoneTags(),
                    zoneRecord.getZoneColor(),
                    zoneRecord.getZoneBorder()
            );
            zones.add(new LandZone(data));
        }
        return zones;
    }

    public static List<LandHistory> splitLandRecordByLand(List<LandHistoryRecord> r){
        if(r != null){
            List<LandHistoryRecord> records = new ArrayList<>(r);
            List<List<LandHistoryRecord>> recordsList = new ArrayList<>();
            List<LandHistoryRecord> tempRecordsList;
            LandHistoryRecord tempRecord;
            boolean exist;
            for(LandHistoryRecord record : records){
                if(record != null){
                    exist = false;
                    for(List<LandHistoryRecord> tempRecords : recordsList){
                        tempRecord = tempRecords.get(0);
                        if(tempRecord.getLandData().getLandID() == record.getLandData().getLandID()){
                            tempRecords.add(record);
                            exist = true;
                            break;
                        }
                    }

                    if(!exist){
                        tempRecordsList = new ArrayList<>();
                        tempRecordsList.add(record);
                        recordsList.add(tempRecordsList);
                    }
                }
            }
            List<LandHistory> result = new ArrayList<>();
            for(List<LandHistoryRecord> dataRecordList:recordsList){
                result.add(new LandHistory(dataRecordList));
            }
            return result;
        }
        return new ArrayList<>();
    }

    public static PolygonOptions getPolygonOptions(
            LandData landData,
            int strokeColor,
            int  fillColor,
            boolean isClickable
    ){
        if(landData == null)
            return null;
        if(landData.getBorder().size() == 0)
            return null;
        PolygonOptions options = new PolygonOptions();
        options.addAll(landData.getBorder());
        options.clickable(isClickable);
        options.strokeColor(strokeColor);
        options.fillColor(fillColor);
        options.zIndex(1);
        for(List<LatLng> hole : landData.getHoles()){
            if(hole.size()>0)
                options.addHole(hole);
        }
        return options;
    }
    public static PolygonOptions getPolygonOptions(
            LandData landData,
            boolean isClickable
    ){
        int strokeColor = Color.argb(
                AppValues.defaultStrokeAlpha,
                landData.getColor().getRed(),
                landData.getColor().getGreen(),
                landData.getColor().getBlue()
        );
        int fillColor = Color.argb(
                AppValues.defaultFillAlpha,
                landData.getColor().getRed(),
                landData.getColor().getGreen(),
                landData.getColor().getBlue()
        );
        return getPolygonOptions(landData, strokeColor, fillColor, isClickable);
    }
    public static PolygonOptions getPolygonOptions(
            LandZoneData zoneData,
            boolean isClickable
    ){
        int strokeColor = Color.argb(
                AppValues.defaultStrokeAlpha,
                zoneData.getColor().getRed(),
                zoneData.getColor().getGreen(),
                zoneData.getColor().getBlue()
        );
        int fillColor = Color.argb(
                AppValues.defaultFillAlpha,
                zoneData.getColor().getRed(),
                zoneData.getColor().getGreen(),
                zoneData.getColor().getBlue()
        );
        return getPolygonOptions(zoneData,strokeColor,fillColor,isClickable);
    }
    public static PolygonOptions getPolygonOptions(
            LandZoneData zoneData,
            int strokeColor,
            int  fillColor,
            boolean isClickable
    ){
        if(zoneData == null)
            return null;
        if(zoneData.getBorder().size() == 0)
            return null;
        PolygonOptions options = new PolygonOptions();
        options.addAll(zoneData.getBorder());
        options.clickable(isClickable);
        options.strokeColor(strokeColor);
        options.fillColor(fillColor);
        options.zIndex(2);
        return options;
    }
}
