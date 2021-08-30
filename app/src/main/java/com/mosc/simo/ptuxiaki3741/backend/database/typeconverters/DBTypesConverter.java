package com.mosc.simo.ptuxiaki3741.backend.database.typeconverters;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;

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
    public static UserDBAction userActionValueToUserAction(int ordinal) {
        return UserDBAction.values()[ordinal];
    }
    @TypeConverter
    public static int userActionToUserActionValue(UserDBAction action) {
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
}
