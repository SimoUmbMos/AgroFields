package com.mosc.simo.ptuxiaki3741.backend.file.openxml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class OpenXmlDataBaseOutput {
    private OpenXmlDataBaseOutput(){}

    public static boolean exportDBXLS(
            FileOutputStream outputStream,
            OpenXmlState state
    ) throws IOException {
        if(outputStream == null)
            return false;

        HSSFWorkbook workbook = new HSSFWorkbook();

        //Lands Sheet
        if(state.getLands().size()>0){
            createLandSheetXLS(state.getLands(), workbook);
        }

        //Zones Sheet
        if(state.getZones().size()>0){
            createLandZoneSheetXLS(state.getZones(), workbook);
        }

        //Categories Sheet
        if(state.getCategories().size() > 0){
            createCategoriesSheetXLS(state.getCategories(), workbook);
        }

        //Notifications Sheet
        if(state.getNotifications().size() > 0){
            createNotificationsSheetXLS(state.getNotifications(), workbook);
        }

        workbook.write(outputStream);
        workbook.close();
        return true;
    }
    private static void createLandSheetXLS(List<Land> lands, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        HSSFSheet sheetLand = workbook.createSheet(AppValues.sheetLandName);

        for(Land land:lands){
            if(land == null || land.getData() == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getYear());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getTags());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getColor().toString());

            cell = row.createCell(colNum);
            border = new ArrayList<>(land.getData().getBorder());
            points = new ArrayList<>(land.getData().getHoles());
            points.add(0,border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));
        }
    }
    private static void createLandZoneSheetXLS(List<LandZone> zones, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        HSSFSheet sheetLandZone = workbook.createSheet(AppValues.sheetLandZoneName);

        List<List<LatLng>> points;
        List<LatLng> border;
        for(LandZone zone:zones){
            if(zone == null || zone.getData() == null)
                continue;

            row = sheetLandZone.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getLid());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getNote());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getTags());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getColor().toString());

            cell = row.createCell(colNum);
            points = new ArrayList<>();
            border = new ArrayList<>(zone.getData().getBorder());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));

        }
    }
    private static void createCategoriesSheetXLS(List<CalendarCategory> categories, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        HSSFSheet sheetLand = workbook.createSheet(AppValues.sheetCategoriesName);

        for(CalendarCategory category: categories){
            if(category == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(category.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(category.getName());

            cell = row.createCell(colNum);
            cell.setCellValue(category.getColorData().toString());
        }
    }
    private static void createNotificationsSheetXLS(List<CalendarNotification> notifications, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        HSSFSheet sheetLand = workbook.createSheet(AppValues.sheetNotificationsName);

        for(CalendarNotification notification: notifications){
            if(notification == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getCategoryID());

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getYear());

            cell = row.createCell(colNum++);
            if(notification.getLid() != null){
                cell.setCellValue(notification.getLid());
            }else{
                cell.setCellValue("");
            }

            cell = row.createCell(colNum++);
            if(notification.getZid() != null){
                cell.setCellValue(notification.getZid());
            }else{
                cell.setCellValue("");
            }

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getMessage());

            cell = row.createCell(colNum);
            cell.setCellValue(notification.getDate().getTime());
        }
    }

    public static boolean exportDBXLSX(
            FileOutputStream outputStream,
            OpenXmlState state
    ) throws IOException {
        if(outputStream == null)
            return false;

        XSSFWorkbook workbook = new XSSFWorkbook();

        //Land Sheet
        if(state.getLands().size()>0){
            createLandSheetXLSX(state.getLands(), workbook);
        }

        //Zone Sheet
        if(state.getZones().size()>0){
            createLandZoneSheetXLSX(state.getZones(), workbook);
        }

        //Categories Sheet
        if(state.getCategories().size() > 0){
            createCategoriesSheetXLSX(state.getCategories(), workbook);
        }

        //Notifications Sheet
        if(state.getNotifications().size() > 0){
            createNotificationsSheetXLSX(state.getNotifications(), workbook);
        }

        workbook.write(outputStream);
        workbook.close();
        return true;
    }
    private static void createLandSheetXLSX(List<Land> lands, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        XSSFSheet sheetLand = workbook.createSheet(AppValues.sheetLandName);

        for(Land land:lands){
            if(land == null || land.getData() == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getYear());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getTags());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getColor().toString());

            cell = row.createCell(colNum);
            border = new ArrayList<>(land.getData().getBorder());
            points = new ArrayList<>(land.getData().getHoles());
            points.add(0,border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));
        }
    }
    private static void createLandZoneSheetXLSX(List<LandZone> zones, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        XSSFSheet sheetLandZone = workbook.createSheet(AppValues.sheetLandZoneName);

        List<List<LatLng>> points;
        List<LatLng> border;
        for(LandZone zone:zones){
            if(zone == null || zone.getData() == null)
                continue;

            row = sheetLandZone.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getLid());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getNote());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getTags());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getColor().toString());

            cell = row.createCell(colNum);
            points = new ArrayList<>();
            border = new ArrayList<>(zone.getData().getBorder());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));

        }
    }
    private static void createCategoriesSheetXLSX(List<CalendarCategory> categories, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        XSSFSheet sheetLand = workbook.createSheet(AppValues.sheetCategoriesName);

        for(CalendarCategory category: categories){
            if(category == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(category.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(category.getName());

            cell = row.createCell(colNum);
            cell.setCellValue(category.getColorData().toString());
        }
    }
    private static void createNotificationsSheetXLSX(List<CalendarNotification> notifications, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        XSSFSheet sheetLand = workbook.createSheet(AppValues.sheetNotificationsName);

        for(CalendarNotification notification: notifications){
            if(notification == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getCategoryID());

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getYear());

            cell = row.createCell(colNum++);
            if(notification.getLid() != null){
                cell.setCellValue(notification.getLid());
            }else{
                cell.setCellValue("");
            }

            cell = row.createCell(colNum++);
            if(notification.getZid() != null){
                cell.setCellValue(notification.getZid());
            }else{
                cell.setCellValue("");
            }

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(notification.getMessage());

            cell = row.createCell(colNum);
            cell.setCellValue(notification.getDate().getTime());
        }
    }
}
