package com.mosc.simo.ptuxiaki3741;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.util.MapHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MapHelperAndroidTest {

    private void populateData1(List<LatLng> p) {
        p.add(new LatLng(41.090931,23.546735));
        p.add(new LatLng(41.090632,23.547239));
        p.add(new LatLng(41.091336,23.547808));
        p.add(new LatLng(41.090931,23.546735));
    }
    private void populateData2(List<LatLng> p) {
        p.add(new LatLng(41.091708,23.547153));
        p.add(new LatLng(41.090956,23.547528));
        p.add(new LatLng(41.091877,23.548656));
        p.add(new LatLng(41.092241,23.548162));
        p.add(new LatLng(41.091708,23.547153));
    }
    private void populateData3(List<LatLng> p) {
        p.add(new LatLng(41.092371,23.547772));
        p.add(new LatLng(41.091562,23.551014));
        p.add(new LatLng(41.093325,23.550863));
        p.add(new LatLng(41.092371,23.547772));
    }

    @Test
    public void TestIntersections() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        List<List<LatLng>> intersectionsPolygons = new ArrayList<>(MapHelper.intersections(p1,p2));
        assertEquals(1,intersectionsPolygons.size());
    }

    @Test
    public void TestUnions() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        List<LatLng> unionsPolygons = new ArrayList<>(MapHelper.union(p1,p2));
        assertEquals(8,unionsPolygons.size());
    }

    @Test
    public void TestDifference1() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        List<LatLng> unionsPolygons = new ArrayList<>(MapHelper.difference(p1,p2));
        assertEquals(4,unionsPolygons.size());
    }
    @Test
    public void TestDifference2() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        List<LatLng> unionsPolygons = new ArrayList<>(MapHelper.difference(p2,p1));
        assertEquals(7,unionsPolygons.size());
    }

    @Test
    public void TestEquals1() {
        List<LatLng> p1 = new ArrayList<>();
        populateData1(p1);
        assertTrue(MapHelper.equals(p1, p1));
    }
    @Test
    public void TestEquals2() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        assertFalse(MapHelper.equals(p2, p1));
    }
    @Test
    public void TestEquals3() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData3(p2);
        assertFalse(MapHelper.equals(p2, p1));
    }
    @Test
    public void TestEquals4() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData2(p1);
        populateData3(p2);
        assertFalse(MapHelper.equals(p2, p1));
    }

    @Test
    public void TestDisjoint1() {
        List<LatLng> p1 = new ArrayList<>();
        populateData1(p1);
        assertFalse(MapHelper.disjoint(p1, p1));
    }
    @Test
    public void TestDisjoint2() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        assertFalse(MapHelper.disjoint(p2, p1));
    }
    @Test
    public void TestDisjoint3() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData3(p2);
        assertTrue(MapHelper.disjoint(p2, p1));
    }
    @Test
    public void TestDisjoint4() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData2(p1);
        populateData3(p2);
        assertTrue(MapHelper.disjoint(p2, p1));
    }

    @Test
    public void TestDistanceBetween1() {
        List<LatLng> p1 = new ArrayList<>();
        populateData1(p1);
        assertEquals(0,MapHelper.distanceBetween(p1, p1),0);
    }
    @Test
    public void TestDistanceBetween2() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData2(p2);
        assertEquals(0,MapHelper.distanceBetween(p2, p1),0);
    }
    @Test
    public void TestDistanceBetween3() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData1(p1);
        populateData3(p2);
        assertNotEquals(0,MapHelper.distanceBetween(p2, p1),0);
    }
    @Test
    public void TestDistanceBetween4() {
        List<LatLng> p1 = new ArrayList<>(),p2 = new ArrayList<>();
        populateData2(p1);
        populateData3(p2);
        assertNotEquals(0,MapHelper.distanceBetween(p2, p1),0);
    }

    @Test
    public void TestArea1() {
        List<LatLng> p1 = new ArrayList<>();
        populateData1(p1);
        assertNotEquals(0,MapHelper.area(p1),0);
    }
    @Test
    public void TestArea2() {
        List<LatLng> p1 = new ArrayList<>();
        populateData2(p1);
        assertNotEquals(0,MapHelper.area(p1),0);
    }
    @Test
    public void TestArea3() {
        List<LatLng> p1 = new ArrayList<>();
        populateData3(p1);
        assertNotEquals(0,MapHelper.area(p1),0);
    }

    @Test
    public void TestLength1() {
        List<LatLng> p1 = new ArrayList<>();
        populateData1(p1);
        assertNotEquals(0,MapHelper.length(p1),0);
    }
    @Test
    public void TestLength2() {
        List<LatLng> p1 = new ArrayList<>();
        populateData2(p1);
        assertNotEquals(0,MapHelper.length(p1),0);
    }
    @Test
    public void TestLength3() {
        List<LatLng> p1 = new ArrayList<>();
        populateData3(p1);
        assertNotEquals(0,MapHelper.length(p1),0);
    }

    @Test
    public void TestSimplify1() {
        List<LatLng> p1 = new ArrayList<>();
        populateData1(p1);
        assertEquals(p1.size(),MapHelper.simplify(p1).size());
    }
    @Test
    public void TestSimplify2() {
        List<LatLng> p1 = new ArrayList<>();
        populateData2(p1);
        assertEquals(p1.size(),MapHelper.simplify(p1).size());
    }
    @Test
    public void TestSimplify3() {
        List<LatLng> p1 = new ArrayList<>();
        populateData3(p1);
        assertEquals(p1.size(),MapHelper.simplify(p1).size());
    }

    @Test
    public void TestContains1() {
        List<LatLng> p2 = new ArrayList<>();
        LatLng p = new LatLng(41.090931,23.546735);
        populateData2(p2);
        assertFalse(MapHelper.contains(p,p2));
    }
    @Test
    public void TestContains2() {
        List<LatLng> p2 = new ArrayList<>();
        LatLng p = new LatLng(41.090632,23.547239);
        populateData2(p2);
        assertFalse(MapHelper.contains(p,p2));
    }
    @Test
    public void TestContains3() {
        List<LatLng> p2 = new ArrayList<>();
        LatLng p = new LatLng(41.091336,23.547808);
        populateData2(p2);
        assertTrue(MapHelper.contains(p,p2));
    }
}