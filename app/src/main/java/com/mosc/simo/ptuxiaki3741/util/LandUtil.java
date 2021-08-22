package com.mosc.simo.ptuxiaki3741.util;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandPoint;

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
                        dataRecord.getLandTitle()
                );
                List<LandPoint> points = new ArrayList<>();
                if(record.getLandPoints() != null){
                    for(int i = 0; i < record.getLandPoints().size(); i++){
                        points.add(new LandPoint(
                                dataRecord.getLandID(),
                                record.getLandPoints().get(i).getPosition(),
                                record.getLandPoints().get(i).getLatLng()
                        ));
                    }
                }
                return new Land(data,points);
            }
        }
        return null;
    }

    public static List<List<LandRecord>> splitLandRecordByLand(List<LandRecord> r){
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

    public static List<LatLng> getLatLngPoints(Land land){
        List<LatLng> points = new ArrayList<>();
        if(land != null){
            if(land.getBorder() != null){
                for (int i = 0; i < land.getBorder().size();i++){
                    points.add(land.getBorder().get(i).getLatLng());
                }
            }
        }
        return points;
    }
}
