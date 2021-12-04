package com.mosc.simo.ptuxiaki3741.file.openxml;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.AppRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpenXmlDataBaseExporter {
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

    private static void createLandSheet(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        XSSFSheet sheetLand = workbook.createSheet("Lands");
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
            cell.setCellValue(holesPrettyPrint(points));
        }
    }
    private static void createLandRecordSheet(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        List<List<LatLng>> points;
        List<LatLng> border;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        XSSFSheet sheetLandRecord = workbook.createSheet("Land Records");
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
            cell.setCellValue(sdf.format(record.getDate()));

            cell = row.createCell(colNum);
            border = new ArrayList<>(record.getBorder());
            points = new ArrayList<>(record.getHoles());
            points.add(border);
            cell.setCellValue(holesPrettyPrint(points));
        }
    }
    private static void createLandZoneSheet(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;
        XSSFSheet sheetLandZone = workbook.createSheet("Zones");
        List<LandZone> zones = repository.getLandZones();
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
            cell.setCellValue(borderPrettyPrint(zone.getData().getBorder()));

        }
    }
    private static void createContactSheet(AppRepository repository, XSSFWorkbook workbook) {
        Row row;
        Cell cell;
        int colNum;
        int rowNum = 0;

        XSSFSheet sheetContact = workbook.createSheet("Contacts");
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

    private static String borderPrettyPrint(List<LatLng> border){
        List<double[]> ans = new ArrayList<>();
        for(LatLng point:border){
            ans.add(new double[]{point.longitude,point.latitude});
        }
        if(border.size()>1){
            if(!border.get(0).equals(border.get(border.size()-1))){
                ans.add(new double[]{border.get(0).longitude,border.get(0).latitude});
            }
        }
        return new Gson().toJson(ans);
    }
    private static String holesPrettyPrint(List<List<LatLng>> holes){
        List<List<double[]>> ans = new ArrayList<>();
        List<double[]> row;
        for(List<LatLng> hole:holes){
            row = new ArrayList<>();
            for(LatLng point : hole){
                row.add(new double[]{point.longitude,point.latitude});
            }
            if(hole.size()>1){
                if(!hole.get(0).equals(hole.get(hole.size()-1))){
                    row.add(new double[]{hole.get(0).longitude,hole.get(0).latitude});
                }
            }
            ans.add(row);
        }
        return new Gson().toJson(ans);
    }
}
