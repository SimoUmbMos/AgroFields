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
import com.mosc.simo.ptuxiaki3741.data.enums.AreaMetrics;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    public static List<String> splitTags(String in){
        List<String> ans = new ArrayList<>();
        if(in == null || in.isEmpty()){
            ans.add(null);
            return ans;
        }
        String[] tags = in.split(",");
        for(String tag : tags){
            String comp = tag;
            if(comp == null || comp.isEmpty()) continue;
            comp = comp.trim();
            if(!ans.contains(comp)) ans.add(comp);
        }
        if(ans.size() == 0) ans.add(null);
        return ans;
    }
    public static String mergeTags(List<String> tags){
        if(tags == null) return "";

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < tags.size(); i++){
            builder.append(tags.get(i));
            if(i != (tags.size() - 1)) builder.append(", ");
        }
        return builder.toString();
    }
    public static String removeSpecialCharacters(String string){
        if(string == null) return "";
        String ans = string.replaceAll(
                "[@#€_&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√÷×¶<>,.]",
                " "
        ).trim();
        return ans.replaceAll(" +", " ");
    }
    public static String removeSpecialCharactersWithoutSpaces(String string) {
        if(string == null) return "";
        String ans = string.replaceAll(
                "[@#€_&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√÷×¶<>,.]",
                " "
        ).trim();
        return ans.replaceAll(" +", "_");
    }
    public static String removeSpecialCharactersCSV(String string){
        if(string == null) return "";
        String ans = string
                .replaceAll("[@#€&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√÷×¶<>.]", " ")
                .replaceAll("_+", " ")
                .replaceAll("( *),( *)", ",")
                .trim();
        return ans.replaceAll(" +", "_");
    }
    public static boolean isColor(String string){
        if(string == null) return false;
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
        if(p == null) return new ArrayList<>();
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
                return land1.getData().getId() == land2.getData().getId();
            }
        }
        return false;
    }
    public static boolean checkItemsTheSame(LandZone zone1, LandZone zone2){
        if(zone1 != null && zone2 != null) {
            if(zone1.getData() != null && zone2.getData() != null) {
                return zone1.getData().getId() == zone2.getData().getId();
            }
        }
        return false;
    }
    public static boolean checkItemsTheSame(CalendarEntity not1, CalendarEntity not2) {
        if(not1 == null || not2 == null) return false;
        return not1.getNotification().getId() == not2.getNotification().getId();
    }

    public static List<LatLng> formatZonePoints(List<LatLng> currentZone, Land land, List<LandZone> otherZones) {
        if(land == null || land.getData() == null || currentZone == null) return new ArrayList<>();

        List<LatLng> border = DataUtil.removeSamePointStartEnd(currentZone);

        List<LatLng> temp = new ArrayList<>(MapUtil.getBiggerAreaZoneIntersections(border,land.getData().getBorder()));
        border.clear();
        border.addAll(temp);
        if(border.size() > 2 && otherZones != null){
            for(LandZone zone : otherZones){
                if(zone == null || zone.getData() == null || zone.getData().getBorder() == null) continue;
                if(MapUtil.containsAll(border,zone.getData().getBorder())){
                    border.clear();
                    break;
                }
                temp.clear();
                temp.addAll(MapUtil.getBiggerAreaZoneDifference(border,zone.getData().getBorder()));
                border.clear();
                border.addAll(temp);
                if(border.size() < 3) break;
            }
        }
        if(border.size() > 2){
            for(List<LatLng> hole : land.getData().getHoles()){
                if(hole == null) continue;
                if(MapUtil.containsAll(border,hole)){
                    border.clear();
                    break;
                }
                List<LatLng> tempBorder = new ArrayList<>(MapUtil.getBiggerAreaZoneDifference(border,hole));
                border.clear();
                border.addAll(tempBorder);
                if(border.size() < 3) break;
            }
        }
        if(border.size() < 3) border.clear();
        return border;
    }

    public static String getAreaString(Context context, double area) {
        AreaMetrics metric = DataUtil.getAreaMetric(area);
        String metricSymbol = DataUtil.getAreaMetricSymbol(context,metric);
        String displayArea = new DecimalFormat("#.###").format(area * metric.dimensionToSquareMeter);
        if(!metricSymbol.isEmpty()){
            displayArea += " " + metricSymbol;
        }
        return displayArea;
    }
    private static String getAreaMetricSymbol(Context context,AreaMetrics metric){
        if(context == null || metric == null) return "";
        switch (metric){
            case SquareFoot: return context.getString(R.string.square_foot_symbol);
            case SquareYard: return context.getString(R.string.square_yard_symbol);
            case SquareMeter: return context.getString(R.string.square_meter_symbol);
            case Stremma: return context.getString(R.string.stremma_symbol);
            case Hectare: return context.getString(R.string.hectare_symbol);
            case Acres: return context.getString(R.string.acres_symbol);
            case SquareKiloMeter: return context.getString(R.string.square_kilometer_symbol);
            case SquareMile: return context.getString(R.string.square_mile_symbol);
            default: return "";
        }
    }
    private static AreaMetrics getAreaMetric(final double area){
        final Locale locale = Locale.getDefault();
        if(locale.equals(Locale.UK) || locale.equals(Locale.US) || locale.getLanguage().equals("my") || locale.getLanguage().equals("vai")){
            double acArea = area * AreaMetrics.Acres.dimensionToSquareMeter;
            double ydArea = area * AreaMetrics.SquareYard.dimensionToSquareMeter;
            if(acArea > 1.0){
                return AreaMetrics.Acres;
            }else if(ydArea > 1.0){
                return AreaMetrics.SquareYard;
            }else {
                return AreaMetrics.SquareFoot;
            }
        }else if(locale.getLanguage().equals("el")){
            double stArea = area * AreaMetrics.Stremma.dimensionToSquareMeter;
            if(stArea > 1.0){
                return AreaMetrics.Stremma;
            }else{
                return AreaMetrics.SquareMeter;
            }
        }else{
            double heArea = area * AreaMetrics.Hectare.dimensionToSquareMeter;
            if(heArea > 1.0){
                return AreaMetrics.Hectare;
            }else{
                return AreaMetrics.SquareMeter;
            }
        }
    }
}
