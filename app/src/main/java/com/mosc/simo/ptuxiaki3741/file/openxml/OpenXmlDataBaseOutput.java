package com.mosc.simo.ptuxiaki3741.file.openxml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
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

    public static boolean exportDBXLS(FileOutputStream outputStream, AppRepository repository) throws IOException {
        if(outputStream == null)
            return false;

        HSSFWorkbook workbook = new HSSFWorkbook();

        //Land Sheet
        createLandSheetXLS(repository, workbook);

        //Zone Sheet
        createLandZoneSheetXLS(repository, workbook);

        //Contact Sheet
        createContactSheetXLS(repository, workbook);

        //Land Record Sheet
        //createLandRecordSheetXLS(repository, workbook);

        workbook.write(outputStream);
        workbook.close();
        return true;
    }
    private static void createLandSheetXLS(AppRepository repository, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        HSSFSheet sheetLand = workbook.createSheet(AppValues.sheetLandName);
        List<Land> lands = repository.getLands();
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

            cell = row.createCell(colNum);
            border = new ArrayList<>(land.getData().getBorder());
            points = new ArrayList<>(land.getData().getHoles());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));
        }
    }
    private static void createLandRecordSheetXLS(AppRepository repository, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        HSSFSheet sheetLandRecord = workbook.createSheet(AppValues.sheetLandRecordName);
        List<LandDataRecord> records = repository.getLandRecords();
        for(LandDataRecord record:records){
            if(record == null)
                continue;

            row = sheetLandRecord.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getLandID());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getLandTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getLandColor().toString());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getActionID().name());

            cell = row.createCell(colNum++);
            cell.setCellValue(DataUtil.printDate(record.getDate()));

            cell = row.createCell(colNum);
            border = new ArrayList<>(record.getBorder());
            points = new ArrayList<>(record.getHoles());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));
        }
    }
    private static void createLandZoneSheetXLS(AppRepository repository, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        HSSFSheet sheetLandZone = workbook.createSheet(AppValues.sheetLandZoneName);
        List<LandZone> zones = repository.getLandZones();
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
            cell.setCellValue(zone.getData().getColor().toString());

            cell = row.createCell(colNum);
            points = new ArrayList<>();
            border = new ArrayList<>(zone.getData().getBorder());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));

        }
    }
    private static void createContactSheetXLS(AppRepository repository, HSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;

        HSSFSheet sheetContact = workbook.createSheet(AppValues.sheetContactName);
        List<Contact> contacts = repository.getContacts();
        for(Contact contact:contacts){
            if(contact == null)
                continue;

            row = sheetContact.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(contact.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(contact.getUsername());

            cell = row.createCell(colNum++);
            cell.setCellValue(contact.getEmail());

            cell = row.createCell(colNum);
            cell.setCellValue(contact.getPhone());
        }
    }

    public static boolean exportDBXLSX(FileOutputStream outputStream, AppRepository repository) throws IOException {
        if(outputStream == null)
            return false;

        XSSFWorkbook workbook = new XSSFWorkbook();

        //Land Sheet
        createLandSheetXLSX(repository, workbook);

        //Zone Sheet
        createLandZoneSheetXLSX(repository, workbook);

        //Contact Sheet
        createContactSheetXLSX(repository, workbook);

        //Land Record Sheet
        //createLandRecordSheetXLSX(repository, workbook);

        workbook.write(outputStream);
        workbook.close();
        return true;
    }
    private static void createLandSheetXLSX(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        XSSFSheet sheetLand = workbook.createSheet(AppValues.sheetLandName);

        List<Land> lands = repository.getLands();
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

            cell = row.createCell(colNum);
            border = new ArrayList<>(land.getData().getBorder());
            points = new ArrayList<>(land.getData().getHoles());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));
        }
    }
    private static void createLandRecordSheetXLSX(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        XSSFSheet sheetLandRecord = workbook.createSheet(AppValues.sheetLandRecordName);

        List<LandDataRecord> records = repository.getLandRecords();
        for(LandDataRecord record:records){
            if(record == null)
                continue;

            row = sheetLandRecord.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getLandID());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getLandTitle());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getLandColor().toString());

            cell = row.createCell(colNum++);
            cell.setCellValue(record.getActionID().name());

            cell = row.createCell(colNum++);
            cell.setCellValue(DataUtil.printDate(record.getDate()));

            cell = row.createCell(colNum);
            border = new ArrayList<>(record.getBorder());
            points = new ArrayList<>(record.getHoles());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));
        }
    }
    private static void createLandZoneSheetXLSX(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        XSSFSheet sheetLandZone = workbook.createSheet(AppValues.sheetLandZoneName);

        List<LandZone> zones = repository.getLandZones();
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
            cell.setCellValue(zone.getData().getColor().toString());

            cell = row.createCell(colNum);
            points = new ArrayList<>();
            border = new ArrayList<>(zone.getData().getBorder());
            points.add(border);
            cell.setCellValue(DataUtil.pointsPrettyPrint(points));

        }
    }
    private static void createContactSheetXLSX(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        XSSFSheet sheetContact = workbook.createSheet(AppValues.sheetContactName);
        List<Contact> contacts = repository.getContacts();
        for(Contact contact:contacts){
            if(contact == null)
                continue;

            row = sheetContact.createRow(rowNum++);
            colNum = 0;

            cell = row.createCell(colNum++);
            cell.setCellValue(contact.getId());

            cell = row.createCell(colNum++);
            cell.setCellValue(contact.getUsername());

            cell = row.createCell(colNum++);
            cell.setCellValue(contact.getEmail());

            cell = row.createCell(colNum);
            cell.setCellValue(contact.getPhone());
        }
    }


}
