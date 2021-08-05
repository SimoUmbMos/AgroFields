package com.mosc.simo.ptuxiaki3741.backend.database.roomserver.typeconverters;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;

import java.util.Date;

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
}
