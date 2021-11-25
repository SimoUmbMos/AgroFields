package com.mosc.simo.ptuxiaki3741;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TestPoly {
    @Test
    public void test1_unite(){
        List<LatLng> poly1 = new ArrayList<>();
        poly1.add(new LatLng(41.076785371944744,23.55361931025982));
        poly1.add(new LatLng(41.076776020523546,23.556030616164207));
        poly1.add(new LatLng(41.073649791131466,23.55663042515516));
        poly1.add(new LatLng(41.07382848736658,23.55338528752327));

        List<LatLng> poly2 = new ArrayList<>();
        poly2.add(new LatLng(41.07532577591344,23.554749526083466));
        poly2.add(new LatLng(41.07408730538049,23.554790429770947));
        poly2.add(new LatLng(41.07407820632699,23.5561553388834));
        poly2.add(new LatLng(41.07533816050097,23.556102365255356));

        LandData result = LandUtil.uniteLandData(
                new LandData(poly1,new ArrayList<>()),
                new LandData(poly2,new ArrayList<>())
        );
        assertNotNull(result);

        StringBuilder display = new StringBuilder();
        for(LatLng point:result.getBorder()){
            display.append(point.latitude).append(",").append(point.longitude).append(" ");
        }
        Log.d("TestPoly", "result border: "+display.toString());
        assertNotEquals(0,result.getBorder().size());

        display.setLength(0);
        for(List<LatLng> hole:result.getHoles()){
            for(LatLng point:hole){
                display.append(point.latitude).append(",").append(point.longitude).append(" ");
            }
        }
        Log.d("TestPoly", "result holes: "+display.toString());
        assertEquals(0, result.getHoles().size());
    }
    @Test
    public void test2_subtract(){
        List<LatLng> poly1 = new ArrayList<>();
        poly1.add(new LatLng(41.076785371944744,23.55361931025982));
        poly1.add(new LatLng(41.076776020523546,23.556030616164207));
        poly1.add(new LatLng(41.073649791131466,23.55663042515516));
        poly1.add(new LatLng(41.07382848736658,23.55338528752327));

        List<LatLng> poly2 = new ArrayList<>();
        poly2.add(new LatLng(41.07532577591344,23.554749526083466));
        poly2.add(new LatLng(41.07408730538049,23.554790429770947));
        poly2.add(new LatLng(41.07407820632699,23.5561553388834));
        poly2.add(new LatLng(41.07533816050097,23.556102365255356));

        LandData result = LandUtil.subtractLandData(
                new LandData(poly1,new ArrayList<>()),
                new LandData(poly2,new ArrayList<>())
        );
        assertNotNull(result);
        StringBuilder display = new StringBuilder();
        for(LatLng point:result.getBorder()){
            display.append(point.latitude).append(",").append(point.longitude).append(" ");
        }
        Log.d("TestPoly", "result border: "+display.toString());
        assertNotEquals(0, result.getBorder().size());

        display.setLength(0);
        for(List<LatLng> hole:result.getHoles()){
            for(LatLng point:hole){
                display.append(point.latitude).append(",").append(point.longitude).append(" ");
            }
        }
        Log.d("TestPoly", "result holes: "+display.toString());
        assertNotEquals(0, result.getHoles().size());
    }
}
