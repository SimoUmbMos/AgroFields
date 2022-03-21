package com.mosc.simo.ptuxiaki3741;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.data.util.MapUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AndroidTestMap {
    private final List<LatLng> points1 = new ArrayList<>();
    private final List<LatLng> points2 = new ArrayList<>();
    private final List<LatLng> points3 = new ArrayList<>();
    private final List<LatLng> points4 = new ArrayList<>();

    @Before
    public void setup() {
        points1.add(new LatLng(41.095722, 23.525871));
        points1.add(new LatLng(41.105722, 23.525871));
        points1.add(new LatLng(41.105722, 23.535871));
        points1.add(new LatLng(41.095722, 23.535871));

        points2.add(new LatLng(41.096830, 23.527599));
        points2.add(new LatLng(41.096930, 23.527699));
        points2.add(new LatLng(41.096930, 23.527699));
        points2.add(new LatLng(41.096830, 23.527599));

        points3.add(new LatLng(43.096830, 25.527599));
        points3.add(new LatLng(43.096840, 25.627599));
        points3.add(new LatLng(43.096840, 25.527609));
        points3.add(new LatLng(43.096830, 25.527609));

        points4.add(new LatLng(41.096741, 23.527395));
        points4.add(new LatLng(41.093297, 23.527395));
        points4.add(new LatLng(41.093297, 23.523951));
        points4.add(new LatLng(41.096741, 23.523951));
    }

    @Test
    public void test1(){
        List<LatLng> points1 = new ArrayList<>(this.points1);
        List<LatLng> points2 = new ArrayList<>(this.points2);
        List<LatLng> points3 = new ArrayList<>(this.points3);

        boolean equal1 = MapUtil.contains(points1, points2);
        boolean equal2 = MapUtil.contains(points1, points3);
        boolean equal3 = MapUtil.contains(points2, points1);
        boolean equal4 = MapUtil.contains(points2, points3);
        boolean equal5 = MapUtil.contains(points3, points1);
        boolean equal6 = MapUtil.contains(points3, points2);

        assertFalse(equal1);
        assertFalse(equal2);
        assertTrue(equal3);
        assertFalse(equal4);
        assertFalse(equal5);
        assertFalse(equal6);
    }

    @Test
    public void test2(){
        List<LatLng> points1 = new ArrayList<>(this.points1);
        List<LatLng> points2 = new ArrayList<>(this.points2);
        List<LatLng> points3 = new ArrayList<>(this.points3);
        List<LatLng> points4 = new ArrayList<>(this.points4);

        boolean equal1 = MapUtil.containsAll(points1, points2);
        boolean equal2 = MapUtil.containsAll(points1, points3);
        boolean equal3 = MapUtil.containsAll(points1, points4);

        boolean equal4 = MapUtil.containsAll(points2, points1);
        boolean equal5 = MapUtil.containsAll(points2, points3);
        boolean equal6 = MapUtil.containsAll(points2, points4);

        boolean equal7 = MapUtil.containsAll(points3, points1);
        boolean equal8 = MapUtil.containsAll(points3, points2);
        boolean equal9 = MapUtil.containsAll(points3, points4);

        boolean equal10 = MapUtil.containsAll(points4, points1);
        boolean equal11 = MapUtil.containsAll(points4, points2);
        boolean equal12 = MapUtil.containsAll(points4, points3);

        assertTrue(equal1);
        assertFalse(equal2);
        assertFalse(equal3);
        assertFalse(equal4);
        assertFalse(equal5);
        assertFalse(equal6);
        assertFalse(equal7);
        assertFalse(equal8);
        assertFalse(equal9);
        assertFalse(equal10);
        assertFalse(equal11);
        assertFalse(equal12);
    }

    @Test
    public void test3(){
        List<LatLng> points1 = new ArrayList<>(this.points1);
        List<LatLng> points2 = new ArrayList<>(this.points2);
        List<LatLng> diff1 = MapUtil.getBiggerAreaZoneDifference(points1,points2);

        points1 = new ArrayList<>(this.points1);
        points2 = new ArrayList<>(this.points2);

        List<LatLng> diff2 = MapUtil.getBiggerAreaZoneDifference(points2,points1);

        points1 = new ArrayList<>(this.points1);

        assertEquals(4,diff1.size());
        assertEquals(0,diff2.size());
        assertTrue(ListUtils.arraysMatch(diff1,points1));
    }
}
