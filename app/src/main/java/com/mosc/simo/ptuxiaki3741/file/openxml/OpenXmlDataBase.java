package com.mosc.simo.ptuxiaki3741.file.openxml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class OpenXmlDataBase {
    private static final String TAG = "OpenXmlDataBaseImport";
    private OpenXmlDataBase(){}

    public static boolean exportDB(FileOutputStream outputStream, AppRepository repository) throws IOException {
        if(outputStream == null)
            return false;

        XSSFWorkbook workbook = new XSSFWorkbook();

        //Land Sheet
        createLandSheet(repository, workbook);

        //Zone Sheet
        createLandZoneSheet(repository, workbook);

        //Contact Sheet
        createContactSheet(repository, workbook);

        //Land Record Sheet
        createLandRecordSheet(repository, workbook);

        workbook.write(outputStream);
        workbook.close();
        return true;
    }
    public static boolean importDB(FileInputStream inputStream, AppRepository repository) throws IOException {
        if(repository == null)
            return false;
        if(inputStream == null)
            return false;

        List<Land> lands = new ArrayList<>();
        List<LandZone> zones = new ArrayList<>();
        List<LandDataRecord> records = new ArrayList<>();
        List<Contact> contacts = new ArrayList<>();

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        for(Sheet sheet : workbook){
            String sheetName = sheet.getSheetName().toLowerCase().trim();
            if(sheetName.equals(AppValues.sheetLandName.toLowerCase())){
                getLandData(sheet, lands);
            }else if(sheetName.equals(AppValues.sheetLandRecordName.toLowerCase())){
                getLandRecordData(sheet, records);
            }else if(sheetName.equals(AppValues.sheetLandZoneName.toLowerCase())){
                getLandZoneData(sheet, zones);
            }else if(sheetName.equals(AppValues.sheetContactName.toLowerCase())){
                getContactData(sheet, contacts);
            }
        }
        workbook.close();

        for(Land land:lands){
            Log.d(TAG, "imported land: "+
                    land.getData().getId()+","+
                    land.getData().getTitle()+","+
                    land.getData().getColor());
        }
        for(LandZone zone:zones){
            Log.d(TAG, "imported zone: "+
                    zone.getData().getId()+","+
                    zone.getData().getLid()+","+
                    zone.getData().getTitle()+","+
                    zone.getData().getColor());
        }
        for(LandDataRecord record:records){
            Log.d(TAG, "imported record: "+
                    record.getId()+","+
                    record.getLandID()+","+
                    record.getLandTitle()+","+
                    record.getLandColor()+","+
                    record.getActionID().name()+","+
                    DataUtil.printDate(record.getDate())
            );
        }
        for(Contact contact:contacts){
            Log.d(TAG, "imported contact: "+
                    contact.getId()+","+
                    contact.getUsername()+","+
                    contact.getEmail()+","+
                    contact.getPhone());
        }

        return true;
    }

    private static void createLandSheet(AppRepository repository, XSSFWorkbook workbook) {
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
    private static void createLandRecordSheet(AppRepository repository, XSSFWorkbook workbook) {
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
    private static void createLandZoneSheet(AppRepository repository, XSSFWorkbook workbook) {
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
    private static void createContactSheet(AppRepository repository, XSSFWorkbook workbook) {
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

    private static void getLandData(Sheet sheet, List<Land> lands) {
        long id;
        String title, cellValue;
        ColorData color;
        for (Row row : sheet) {
            id = -1;
            title = "";
            color = null;

            for (Cell cell : row) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    id = Double.valueOf(cell.getNumericCellValue()).longValue();
                } else if (cell.getCellType() == CellType.STRING) {
                    cellValue = cell.getStringCellValue().trim();
                    if(DataUtil.isColor(cellValue)){
                        color = new ColorData(cellValue);
                    }else{
                        title = cellValue;
                    }
                }
            }

            if(id != -1 && !title.isEmpty() && color != null){
                lands.add(new Land(new LandData(id,title,color,new ArrayList<>(),new ArrayList<>())));
            }
        }
    }
    private static void getLandZoneData(Sheet sheet, List<LandZone> zones) {
        long id, lid;
        String title, cellValue;
        ColorData color;
        for (Row row : sheet) {
            id = -1;
            lid = -1;
            title = "";
            color = null;

            for (Cell cell : row) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    if(id == -1){
                        id = Double.valueOf(cell.getNumericCellValue()).longValue();
                    }else if(lid == -1){
                        lid = Double.valueOf(cell.getNumericCellValue()).longValue();
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    cellValue = cell.getStringCellValue().trim();
                    if(DataUtil.isColor(cellValue)){
                        color = new ColorData(cellValue);
                    }else{
                        title = cellValue;
                    }
                }
            }

            if(id != -1 && lid != -1 && !title.isEmpty() && color != null){
                zones.add(new LandZone(new LandZoneData(id,lid,title,color,new ArrayList<>())));
            }
        }
    }
    private static void getLandRecordData(Sheet sheet, List<LandDataRecord> records) {
        long id, lid;
        String title, cellValue;
        ColorData color;
        LandDBAction action;
        Date date;
        for (Row row : sheet) {
            id = -1;
            lid = -1;
            title = "";
            color = null;
            action = null;
            date = null;

            for (Cell cell : row) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    if(id == -1){
                        id = Double.valueOf(cell.getNumericCellValue()).longValue();
                    }else if(lid == -1){
                        lid = Double.valueOf(cell.getNumericCellValue()).longValue();
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    cellValue = cell.getStringCellValue().trim();
                    if(DataUtil.isColor(cellValue)){
                        color = new ColorData(cellValue);
                    }else if(DataUtil.isLandDBAction(cellValue)){
                        action = DataUtil.getLandDBAction(cellValue);
                    }else if(DataUtil.isDate(cellValue)){
                        date = DataUtil.getDate(cellValue);
                    }else{
                        title = cellValue;
                    }
                }
            }

            if(id != -1 && lid != -1 && !title.isEmpty() && color != null && action != null && date != null){
                records.add(new LandDataRecord(id, lid,title,color,action,date,new ArrayList<>(),new ArrayList<>()));
            }
        }
    }
    private static void getContactData(Sheet sheet, List<Contact> contacts) {
        long id;
        String username, email, phone, cellValue;
        for (Row row : sheet) {
            id = -1;
            username = "";
            email = "";
            phone = "";

            for (Cell cell : row) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    id = Double.valueOf(cell.getNumericCellValue()).longValue();
                } else if (cell.getCellType() == CellType.STRING) {
                    cellValue = cell.getStringCellValue().trim();
                    if(DataUtil.isEmail(cellValue)){
                        email = cellValue;
                    }else if(DataUtil.isPhone(cellValue)){
                        phone = cellValue;
                    }else{
                        username = cellValue;
                    }
                }
            }

            if (id != -1 && !username.isEmpty()) {
                contacts.add(new Contact(id, username, email, phone));
            }
        }
    }


}
