package com.mosc.simo.ptuxiaki3741.file.openxml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

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
            List<Land> lands,
            List<LandZone> zones
    ) throws IOException {
        if(outputStream == null)
            return false;

        HSSFWorkbook workbook = new HSSFWorkbook();

        //Land Sheet
        if(lands.size()>0){
            createLandSheetXLS(lands, workbook);
        }

        //Zone Sheet
        if(zones.size()>0){
            createLandZoneSheetXLS(zones, workbook);
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
            if(land == null)
                continue;

            if(land.getData() == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getColor().toString());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getSnapshot());

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
            if(zone == null)
                continue;
            if(zone.getData() == null)
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
            cell.setCellValue(zone.getData().getColor().toString());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getSnapshot());

            cell = row.createCell(colNum);
            points = new ArrayList<>();
            border = new ArrayList<>(zone.getData().getBorder());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));

        }
    }

    public static boolean exportDBXLSX(
            FileOutputStream outputStream,
            List<Land> lands,
            List<LandZone> zones
    ) throws IOException {
        if(outputStream == null)
            return false;

        XSSFWorkbook workbook = new XSSFWorkbook();

        //Land Sheet
        if(lands.size()>0){
            createLandSheetXLSX(lands, workbook);
        }

        //Zone Sheet
        if(zones.size()>0){
            createLandZoneSheetXLSX(zones, workbook);
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
            if(land == null)
                continue;

            if(land.getData() == null)
                continue;

            row = sheetLand.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getColor().toString());

            cell = row.createCell(colNum++);
            cell.setCellValue(land.getData().getSnapshot());

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
            if(zone == null)
                continue;
            if(zone.getData() == null)
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
            cell.setCellValue(zone.getData().getColor().toString());

            cell = row.createCell(colNum++);
            cell.setCellValue(zone.getData().getSnapshot());

            cell = row.createCell(colNum);
            points = new ArrayList<>();
            border = new ArrayList<>(zone.getData().getBorder());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));

        }
    }
}
