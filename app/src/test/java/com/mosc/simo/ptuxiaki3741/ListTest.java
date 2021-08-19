package com.mosc.simo.ptuxiaki3741;

import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListTest {
    List<Land> lands;
    List<LandRecord> landRecords;

    private List<Land> getLandsList(){
        return lands;
    }
    private List<LandRecord> getLandsHistoryList(){
        return landRecords;
    }

    public List<Object> merge(){
        List<Object> result = new ArrayList<>();
        List<Land> lands = new ArrayList<>(getLandsList());
        List<LandRecord> landRecords = new ArrayList<>(getLandsHistoryList());
        for (Iterator<Land> i = lands.iterator(); i.hasNext();) {
            Land land = i.next();
            if(land.getData() != null){
                result.add(land);
                for (Iterator<LandRecord> j = landRecords.iterator(); j.hasNext();) {
                    LandRecord landRecord = j.next();
                    if (landRecord.getLandData() != null) {
                        if(landRecord.getLandData().getLandID() == land.getData().getId()){
                            result.add(landRecord);
                            j.remove();
                        }
                    }else{
                        j.remove();
                    }
                }
            }
            i.remove();
        }
        return result;
    }

    @Before
    public void createMocKLists() {
        lands = new ArrayList<>();
        landRecords = new ArrayList<>();
        LandData temp;
        LandDataRecord temp2;
        SecureRandom random = new SecureRandom();
        int recordId = 1,randomRecordNumber;
        for(int i = 1; i <= 20; i++){
            temp = new LandData(i,-1,"test-land-"+i);
            lands.add(new Land(temp));
            randomRecordNumber = random.nextInt(6);
            for(int j = 0; j <= randomRecordNumber; j++){
                if(j == 0){
                    temp2 = new LandDataRecord(recordId,temp, -1, LandDBAction.CREATE, new Date());
                }else if(random.nextBoolean()){
                    temp2 = new LandDataRecord(recordId,temp, -1, LandDBAction.UPDATE, new Date());
                }else{
                    temp2 = new LandDataRecord(recordId,temp, -1, LandDBAction.DELETE, new Date());
                    landRecords.add(new LandRecord(temp2, new ArrayList<>()));
                    recordId++;
                    break;
                }
                landRecords.add(new LandRecord(temp2, new ArrayList<>()));
                recordId++;
            }
        }
    }

    @Test
    public void merge2list(){
        List<Object> union = merge();
        assertEquals(getLandsList().size() + getLandsHistoryList().size(),union.size());

        System.out.println("Lands");
        for(Land land: getLandsList())
            debugLand(land);
        System.out.println();

        System.out.println("LandRecords");
        for(LandRecord landHistory: getLandsHistoryList())
            debugLandHistory(landHistory);
        System.out.println();

        System.out.println("Union");
        for(Object obj : union){
            if(obj instanceof Land)
                debugLand((Land)obj);
            else if(obj instanceof LandRecord)
                debugLandHistory((LandRecord)obj);
        }
        System.out.println();


    }

    private void debugLand(Land land) {
        if(land.getData() != null){
            System.out.println(
                    "Land id: "+land.getData().getId() +
                    " title: "+land.getData().getTitle()
            );
        }
    }
    private void debugLandHistory(LandRecord landHistory) {
        if(landHistory.getLandData() != null){
            String action = "";
            switch (landHistory.getLandData().getActionID()){
                case CREATE:
                    action = "Create";
                    break;
                case UPDATE:
                    action = "Update";
                    break;
                case RESTORE:
                    action = "Restore";
                    break;
                case DELETE:
                    action = "Delete";
                    break;
            }
            System.out.println(
                    "History id: "+landHistory.getLandData().getId() +
                    " lid: "+landHistory.getLandData().getLandID() +
                    " title: "+landHistory.getLandData().getLandTitle() +
                    " action: "+action
            );
        }
    }
}
