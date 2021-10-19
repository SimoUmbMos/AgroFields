package com.mosc.simo.ptuxiaki3741.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.List;

public final class LandUtil {
    private LandUtil(){}

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
                currLandData.getCreator_id(),
                currLandData.getTitle(),
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
            if(ans.size() == 1){
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
                currLandData.getCreator_id(),
                currLandData.getTitle(),
                newBorder,
                holes
        );
    }

    public static LandData getLandDataFromLandRecord(LandDataRecord record){
        if(record != null){
            return new LandData(
                    record.getLandID(),
                    record.getLandCreatorID(),
                    record.getLandTitle(),
                    record.getBorder(),
                    record.getHoles()
            );
        }
        return null;
    }

    public static List<LandHistory> splitLandRecordByLand(List<LandDataRecord> r){
        if(r != null){
            List<LandDataRecord> records = new ArrayList<>(r);
            List<List<LandDataRecord>> recordsList = new ArrayList<>();
            List<LandDataRecord> tempRecordsList;
            LandDataRecord tempRecord;
            boolean exist;
            for(LandDataRecord record : records){
                if(record != null){
                    exist = false;
                    for(List<LandDataRecord> tempRecords : recordsList){
                        tempRecord = tempRecords.get(0);
                        if(tempRecord.getLandID() == record.getLandID()){
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
            for(List<LandDataRecord> dataRecordList:recordsList){
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
        for(List<LatLng> hole : landData.getHoles()){
            if(hole.size()>0)
                options.addHole(hole);
        }
        return options;
    }
}
