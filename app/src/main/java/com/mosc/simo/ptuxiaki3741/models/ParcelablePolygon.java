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
    private double[] lats;
    private double[] lngs;

    public ParcelablePolygon(List<LatLng> points){
        List<Double> mLats = new ArrayList<>();
        List<Double> mLngs = new ArrayList<>();
        for(LatLng point : points){
            mLats.add(point.latitude);
            mLngs.add(point.longitude);
        }
        lats = toPrimitive(mLats);
        lngs = toPrimitive(mLngs);
    }
    protected ParcelablePolygon(Parcel in) {
        int size = in.readInt();
        lats = new double[size];
        lngs = new double[size];
        in.readDoubleArray(lats);
        in.readDoubleArray(lngs);
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
        for (int i = 0; i < lats.length; i++) {
            points.add(new LatLng(lats[i],lngs[i]));
        }
        return points;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lats.length);
        dest.writeDoubleArray(lats);
        dest.writeDoubleArray(lngs);
    }
}
