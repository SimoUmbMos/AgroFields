package com.mosc.simo.ptuxiaki3741.util;

import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.List;

public final class LandUtil {
    private LandUtil(){}

    public static LandData getLandDataFromLandRecord(LandDataRecord record){
        if(record != null){
            return new LandData(
                    record.getLandID(),
                    record.getLandCreatorID(),
                    record.getLandTitle(),
                    record.getBorder()
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

    public static boolean areSameLandHistory(LandHistory oldItem, LandHistory newItem) {
        if(oldItem.getLandData() == null && newItem.getLandData() == null )
            return true;
        if(oldItem.getLandData() == null || newItem.getLandData() == null )
            return false;
        return oldItem.getLandData().getId() == newItem.getLandData().getId();
    }
    public static boolean areSameLandHistoryContent(LandHistory oldItem, LandHistory newItem) {
        return ListUtils.arraysMatch(oldItem.getData(),newItem.getData());
    }
}
