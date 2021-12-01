package com.mosc.simo.ptuxiaki3741.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

public class ColorData implements Parcelable {
    private int red, green, blue;
    public ColorData(){
        setRed(0);
        setGreen(0);
        setBlue(0);
    }
    protected ColorData(Parcel in) {
        setRed(in.readInt());
        setGreen(in.readInt());
        setBlue(in.readInt());
    }
    public ColorData(int red, int green, int blue){
        setRed(red);
        setGreen(green);
        setBlue(blue);
    }

    public int getColor(){
        return Color.rgb(red,green,blue);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void setRed(int red) {
        if(red<0)
            this.red = 0;
        else this.red = Math.min(red, 255);
    }

    public void setGreen(int green) {
        if(green<0)
            this.green = 0;
        else this.green = Math.min(green, 255);
    }

    public void setBlue(int blue) {
        if(blue<0)
            this.blue = 0;
        else this.blue = Math.min(blue, 255);
    }

    public static final Creator<ColorData> CREATOR = new Creator<ColorData>() {
        @Override
        public ColorData createFromParcel(Parcel in) {
            return new ColorData(in);
        }

        @Override
        public ColorData[] newArray(int size) {
            return new ColorData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(red);
        parcel.writeInt(green);
        parcel.writeInt(blue);
    }
}
