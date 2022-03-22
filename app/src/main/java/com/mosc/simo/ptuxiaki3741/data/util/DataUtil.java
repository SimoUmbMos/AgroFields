package com.mosc.simo.ptuxiaki3741.data.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.CalendarEntity;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.ui.broadcast_receivers.CalendarReceiver;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DataUtil {
    private static final String TAG = "DataUtil";
    private DataUtil(){}

    public static ColorData getRandomLandColor(Context context){
        if(context == null) {
            if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                return AppValues.defaultLandColor;
            } else {
                return AppValues.defaultZoneColor;
            }
        }else{
            List<Integer> colors = new ArrayList<>();
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker1));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker2));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker3));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker4));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker5));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker6));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker7));
            colors.add(ContextCompat.getColor(context, R.color.common_colorPicker8));
            return new ColorData(colors.get(ThreadLocalRandom.current().nextInt(0, colors.size())));
        }
    }

    public static int lineCount(String string){
        if(string == null)
            return 1;
        String[] lines = string.split("\r\n|\r|\n");
        return  lines.length;
    }
    public static List<String> splitTags(String in){
        List<String> ans = new ArrayList<>();
        if(in == null || in.isEmpty()){
            ans.add(null);
            return ans;
        }
        String[] tags = in.split(",");
        for(String tag : tags){
            String comp = tag;
            if(comp == null || comp.isEmpty()) comp = null;
            if(comp != null) {
                comp = comp.trim();
            }
            if(!ans.contains(comp)) ans.add(comp);
        }

        return ans;
    }
    public static String removeSpecialCharacters(String string){
        String ans = string.replaceAll(
                "[@#€_&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√π÷×¶∆<>,.]",
                " "
        ).trim();
        return ans.replaceAll(" +", " ");
    }
    public static String removeSpecialCharactersWithoutSpaces(String string) {
        String ans = string.replaceAll(
                "[@#€_&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√π÷×¶∆<>,.]",
                " "
        ).trim();
        return ans.replaceAll(" +", "_");
    }
    public static String removeSpecialCharactersCSV(String string){
        String ans = string
                .replaceAll("[@#€&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√π÷×¶∆<>.]", " ")
                .replaceAll("_+", " ")
                .replaceAll("( *),( *)", ",")
                .trim();
        return ans.replaceAll(" +", "_");
    }
    public static boolean isColor(String string){
        String colorRegex = "^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
        return string.matches(colorRegex);
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
    public static List<LatLng> removeSamePointStartEnd(List<LatLng> p){
        List<LatLng> tempPointList = new ArrayList<>(p);
        boolean continueRemoving = true;
        while (tempPointList.size()>1 && continueRemoving){
            if(tempPointList.get(0).equals(tempPointList.get(tempPointList.size()-1))){
                tempPointList.remove(tempPointList.size()-1);
            }else{
                continueRemoving = false;
            }
        }
        return tempPointList;
    }
    public static String inputSteamToString(InputStream is){
        StringBuilder sb = new StringBuilder();
        String line;
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
        }catch (IOException e){
            Log.e(TAG, "inputSteamToString: ", e);
            sb = null;
        }
        if(sb != null) {
            return sb.toString();
        }
        return "";
    }
    public static void addNotificationToAlertManager(Context ctx, CalendarNotification n){
        if(n == null) return;
        if(Calendar.getInstance().getTimeInMillis() > n.getDate().getTime()) return;

        AlarmManager am = (AlarmManager) ctx.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(ctx, CalendarReceiver.class);
        intent.putExtra(AppValues.argNotification,new CalendarNotification(n));
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
        intent.putExtra(AppValues.argNotification,new CalendarNotification(n));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx,
                (int) n.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pendingIntent);
        Log.d(TAG, "removeNotificationToAlertManager: removed");
    }

    public static boolean checkItemsTheSame(Land land1, Land land2){
        if(land1 != null && land2 != null) {
            if(land1.getData() != null && land2.getData() != null) {
                return land1.getData().getId() == land2.getData().getId() &&
                        land1.getData().getSnapshot() == land2.getData().getSnapshot();
            }
        }
        return false;
    }
    public static boolean checkItemsTheSame(LandZone zone1, LandZone zone2){
        if(zone1 != null && zone2 != null) {
            if(zone1.getData() != null && zone2.getData() != null) {
                return zone1.getData().getId() == zone2.getData().getId() &&
                        zone1.getData().getSnapshot() == zone2.getData().getSnapshot();
            }
        }
        return false;
    }
    public static boolean checkItemsTheSame(CalendarEntity not1, CalendarEntity not2) {
        if(not1 == null || not2 == null) return false;
        return not1.getNotification().getId() == not2.getNotification().getId();
    }
}
