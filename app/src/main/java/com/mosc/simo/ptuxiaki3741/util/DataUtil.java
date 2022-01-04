package com.mosc.simo.ptuxiaki3741.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mosc.simo.ptuxiaki3741.broadcast_receivers.CalendarReceiver;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import org.apache.commons.validator.routines.EmailValidator;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class DataUtil {
    private static final String TAG = "DataUtil";
    private DataUtil(){}

    public static int lineCount(String string){
        if(string == null)
            return 1;
        String[] lines = string.split("\r\n|\r|\n");
        return  lines.length;
    }
    public static String removeSpecialCharacters(String string){
        String ans = string.trim();
        ans = ans.replaceAll(
                "[@#€_&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√π÷×¶∆<>,.]",
                ""
        );
        return ans.replaceAll(" +", " ");
    }
    public static String dividersToSpace(String string){
        String ans = string.replaceAll("[,.\r\n]", " ");
        return ans.replaceAll(" +", " ");
    }
    public static boolean isEmail(String string){
        return EmailValidator.getInstance().isValid(string);
    }
    public static boolean isPhone(String string){
        String phoneRegex = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
        return string.matches(phoneRegex);
    }
    public static boolean isColor(String string){
        String colorRegex = "^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
        return string.matches(colorRegex);
    }
    public static boolean isLandDBAction(String string){
        for(LandDBAction action:LandDBAction.values()){
            if(action.name().equals(string)){
                return true;
            }
        }
        return false;
    }
    public static boolean isDate(String string){
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        );
        try{
            Date date = sdf.parse(string);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public static LandDBAction getLandDBAction(String string){
        for(LandDBAction action:LandDBAction.values()){
            if(action.name().equals(string)){
                return action;
            }
        }
        return null;
    }
    public static Date getDate(String string){
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        );
        Date date;
        try{
            date = sdf.parse(string);
        }catch (Exception e){
            date = null;
        }
        return date;
    }
    public static String printDate(Date date){
        if(date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        );
        return sdf.format(date);
    }
    public static String pointsPrettyPrint(List<List<LatLng>> pointsList){
        List<List<double[]>> ans = new ArrayList<>();
        List<double[]> row;
        for(List<LatLng> points:pointsList){
            row = new ArrayList<>();
            for(LatLng point : points){
                row.add(new double[]{point.longitude,point.latitude});
            }
            if(points.size()>1){
                if(!points.get(0).equals(points.get(points.size()-1))){
                    row.add(new double[]{points.get(0).longitude,points.get(0).latitude});
                }
            }
            ans.add(row);
        }
        return new Gson().toJson(ans);
    }
    public static List<List<LatLng>> pointsFromString(String pointsString){
        Type listType = new TypeToken<List<List<double[]>>>() {}.getType();
        List<List<double[]>> pointsList = new Gson().fromJson(pointsString, listType);

        List<List<LatLng>> ans = new ArrayList<>();
        int size = 0;
        for(List<double[]> points:pointsList){
            ans.add(size,new ArrayList<>());
            for(double[] point:points){
                if(point.length == 2){
                    ans.get(size).add(new LatLng(point[1],point[0]));
                }
            }
            size++;
        }
        return ans;
    }
    public static LocalDate dateToLocalDate(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    public static void addNotificationToAlertManager(Context ctx, CalendarNotification n){
        if(n == null) return;
        if(Calendar.getInstance().getTimeInMillis() > n.getDate().getTime()) return;

        AlarmManager am = (AlarmManager) ctx.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(ctx, CalendarReceiver.class);
        intent.putExtra(AppValues.argNotification,n);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx,
                (int) n.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.setExact(AlarmManager.RTC_WAKEUP, n.getDate().getTime(), pendingIntent);
        Log.d(TAG, "addNotificationToAlertManager: added");
    }
    public static void removeNotificationToAlertManager(Context ctx, CalendarNotification n){
        if(n == null) return;
        if(Calendar.getInstance().getTimeInMillis() > n.getDate().getTime()) return;

        AlarmManager am = (AlarmManager) ctx.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(ctx, CalendarReceiver.class);
        intent.putExtra(AppValues.argNotification,n);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx,
                (int) n.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pendingIntent);
        Log.d(TAG, "removeNotificationToAlertManager: removed");
    }
}
