package com.mosc.simo.ptuxiaki3741.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class ParcelablePolygon implements Parcelable {
    public static final Creator<ParcelablePolygon> CREATOR = new Creator<ParcelablePolygon>() {
        @Override
        public ParcelablePolygon createFromParcel(Parcel in) {
            return new ParcelablePolygon(in);
        }

        @Override
        public ParcelablePolygon[] newArray(int size) {
            return new ParcelablePolygon[size];
        }
    };
    private final double[] latList;
    private final double[] lngList;

    public ParcelablePolygon(List<LatLng> points){
        List<Double> mLatList = new ArrayList<>();
        List<Double> mLngList = new ArrayList<>();
        for(LatLng point : points){
            mLatList.add(point.latitude);
            mLngList.add(point.longitude);
        }
        latList = toPrimitive(mLatList);
        lngList = toPrimitive(mLngList);
    }
    protected ParcelablePolygon(Parcel in) {
        int size = in.readInt();
        latList = new double[size];
        lngList = new double[size];
        in.readDoubleArray(latList);
        in.readDoubleArray(lngList);
    }

    private double[] toPrimitive(List<Double> list){
        double[] res = new double[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public List<LatLng> getPoints(){
        List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < latList.length; i++) {
            points.add(new LatLng(latList[i],lngList[i]));
        }
        return points;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(latList.length);
        dest.writeDoubleArray(latList);
        dest.writeDoubleArray(lngList);
    }
}
