package com.mosc.simo.ptuxiaki3741.util;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.List;

public class LandUtil {
    public static Land getLandFromLandRecord(LandRecord record){
        if(record != null){
            if(record.getLandData() != null){
                LandDataRecord dataRecord = record.getLandData();
                LandData data = new LandData(
                        dataRecord.getLandID(),
                        dataRecord.getLandCreatorID(),
                        dataRecord.getLandTitle(),
                        record.getLandData().getBorder()
                );
                return new Land(data);
            }
        }
        return null;
    }

    public static List<List<LandRecord>> splitLandRecordByLand(List<LandRecord> r){
        if(r != null){
            List<LandRecord> records = new ArrayList<>(r);
            List<List<LandRecord>> recordsList = new ArrayList<>();
            List<LandRecord> tempRecordsList;
            LandRecord tempRecord;
            boolean exist;
            for(LandRecord record : records){
                if(record.getLandData() != null){
                    exist = false;
                    for(List<LandRecord> tempRecords : recordsList){
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
            return recordsList;
        }
        return new ArrayList<>();
    }
}
