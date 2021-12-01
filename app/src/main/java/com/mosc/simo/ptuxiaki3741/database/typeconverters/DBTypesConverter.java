package com.mosc.simo.ptuxiaki3741.database.typeconverters;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class DBTypesConverter {
    @TypeConverter
    public static LandDBAction landActionValueToLandAction(int ordinal) {
        return LandDBAction.values()[ordinal];
    }
    @TypeConverter
    public static int landActionToLandActionValue(LandDBAction action) {
        return action.ordinal();
    }
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    @TypeConverter
    public static List<LatLng> stringToPoints(String points) {
        Type listType = new TypeToken<List<LatLng>>() {}.getType();
        return new Gson().fromJson(points, listType);
    }
    @TypeConverter
    public static String pointsToString(List<LatLng> points) {
        Gson gson = new Gson();
        return gson.toJson(points);
    }
    @TypeConverter
    public static List<List<LatLng>> stringToHolesList(String holes) {
        Type listType = new TypeToken<List<List<LatLng>>>() {}.getType();
        return new Gson().fromJson(holes, listType);
    }
    @TypeConverter
    public static String holesListToString(List<List<LatLng>> holes) {
        Gson gson = new Gson();
        return gson.toJson(holes);
    }
}
