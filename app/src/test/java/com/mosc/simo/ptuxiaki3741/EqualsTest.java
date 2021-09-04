package com.mosc.simo.ptuxiaki3741;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandHistory;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.util.ListUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EqualsTest {
    @Test
    public void testLandEquals(){
        List<LatLng> points = new ArrayList<>();
        List<LatLng> points2 = new ArrayList<>();
        points2.add(new LatLng(0,0));
        List<LatLng> points3 = new ArrayList<>(points2);
        LandData data = new LandData(1,1,"test",points);

        Land land1 = new Land();
        Land land2 = new Land(null);
        Land land3 = new Land(data);
        Land land4 = new Land(new LandData(1,1,"test",points));
        Land land5 = new Land(new LandData(2,1,"test2",points));
        Land land6 = new Land(new LandData(2,1,"test2",points2));
        Land land7 = new Land(new LandData(2,1,"test2",points3));
        Land land8 = new Land(new LandData(1,"test2",points3));

        Assert.assertTrue(land1.equals(land2));
        Assert.assertTrue(land3.equals(land4));
        Assert.assertTrue(land6.equals(land7));

        Assert.assertFalse(land1.equals(land3));
        Assert.assertFalse(land1.equals(land8));
        Assert.assertFalse(land4.equals(land5));
        Assert.assertFalse(land6.equals(land5));
    }
    @Test
    public void testLandHistoryEquals(){
        User user1 = new User(1,"test","test","test","test");
        User user2 = new User(2,"test","test","test","test");
        Land land1 = new Land(new LandData(1,user1.getId(),"test",new ArrayList<>()));
        Land land2 = new Land(new LandData(2,user2.getId(),"test",new ArrayList<>()));

        List<LandDataRecord> records = new ArrayList<>();
        records.add(new LandDataRecord(land1.getData(),user1,LandDBAction.CREATE,new Date()));
        records.add(new LandDataRecord(land1.getData(),user1,LandDBAction.UPDATE,new Date()));
        records.add(new LandDataRecord(land1.getData(),user1,LandDBAction.DELETE,new Date()));
        List<LandDataRecord> recordsCopy = new ArrayList<>(records);

        Assert.assertTrue(ListUtils.arraysMatch(records,recordsCopy));

        List<LandDataRecord> records2 = new ArrayList<>();
        records2.add(new LandDataRecord(land2.getData(),user2,LandDBAction.CREATE,new Date()));
        records2.add(new LandDataRecord(land2.getData(),user2,LandDBAction.UPDATE,new Date()));
        records2.add(new LandDataRecord(land2.getData(),user2,LandDBAction.UPDATE,new Date()));

        LandHistory record1 = new LandHistory();
        LandHistory record2 = new LandHistory(new ArrayList<>());

        LandHistory record3 = new LandHistory(records);
        LandHistory record4 = new LandHistory(recordsCopy);

        LandHistory record6= new LandHistory(records2);

        Assert.assertTrue(record1.equals(record2));
        Assert.assertTrue(record3.equals(record4));

        Assert.assertFalse(record1.equals(record3));
        Assert.assertFalse(record2.equals(record4));
        Assert.assertFalse(record4.equals(record6));
    }
}
