package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.typeconverters;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;

import java.util.Date;

public class DBTypesConverter {
    @TypeConverter
    public static LandDBAction actionValueToAction(int ordinal) {
        return LandDBAction.values()[ordinal];
    }
    @TypeConverter
    public static int actionToActionValue(LandDBAction action) {
        return action.ordinal();
    }
    @TypeConverter
    public static LatLng latLngStringToLatLng(String latLng) {
        String[] latLngArray =  latLng.split(",");
        double latitude = Double.parseDouble(latLngArray[0]);
        double longitude = Double.parseDouble(latLngArray[1]);
        return new LatLng(latitude,longitude);
    }
    @TypeConverter
    public static String latLngToLatLngString(LatLng latLng) {
        return latLng.latitude+","+latLng.longitude;
    }
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
