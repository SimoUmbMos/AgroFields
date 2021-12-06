package com.mosc.simo.ptuxiaki3741.file.openxml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.ColorData;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public final class OpenXmlDataBaseInput {
    private OpenXmlDataBaseInput(){}
    public static boolean importDB(
            File file,
            List<Land> lands,
            List<LandZone> zones,
            List<Contact> contacts
    ){
        try{
            Workbook workbook = WorkbookFactory.create(file);
            readDataFromWorkbook(workbook,lands,zones,contacts);
            return true;
        }catch (Exception ignored) {}

        try{
            OPCPackage container;
            String sheetName;
            container = OPCPackage.open(file);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
            XSSFReader xssfReader = new XSSFReader(container);
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (iter.hasNext()) {
                InputStream stream = iter.next();
                sheetName = iter.getSheetName().toLowerCase().trim();

                switch (sheetName) {
                    case AppValues.sheetLandNameLowerCase:
                        processLandsSheet(strings, stream, lands);
                        break;
                    case AppValues.sheetLandZoneNameLowerCase:
                        processZonesSheet(strings, stream, zones);
                        break;
                    case AppValues.sheetContactNameLowerCase:
                        processContactsSheet(strings, stream, contacts);
                        break;
                    /*
                    case AppValues.sheetLandRecordName:
                        processRecordsSheet(strings, stream, records);
                        break;
                    */
                }

                stream.close();
            }
            return true;
        }catch (Exception ignored){}

        return false;
    }

    private static void readDataFromWorkbook(
            Workbook workbook,
            List<Land> lands,
            List<LandZone> zones,
            List<Contact> contacts
    ) throws IOException {
        for(Sheet sheet : workbook){
            String sheetName = sheet.getSheetName().toLowerCase().trim();
            switch (sheetName){
                case (AppValues.sheetLandNameLowerCase):
                    getLandData(sheet, lands);
                    break;
                case (AppValues.sheetLandZoneNameLowerCase):
                    getLandZoneData(sheet, zones);
                    break;
                case (AppValues.sheetContactNameLowerCase):
                    getContactData(sheet, contacts);
                    break;
            }
        }
        workbook.close();
    }
    private static void getLandData(Sheet sheet, List<Land> lands) {
        long id;
        String title, points;
        ColorData color;
        for (Row row : sheet) {
            id = -1;
            title = "";
            color = null;
            points = "";//FIXME:BORDER -> POINTS

            for (Cell cell : row) {
                switch (cell.getColumnIndex()){
                    case 0:
                        if(cell.getCellType() == CellType.NUMERIC)
                            id = Double.valueOf(cell.getNumericCellValue()).longValue();
                        break;
                    case 1:
                        title = cell.getStringCellValue().trim();
                        break;
                    case 2:
                        color = new ColorData(cell.getStringCellValue().trim());
                        break;
                    case 3:
                        points =  cell.getStringCellValue().trim();
                        break;
                }
            }

            if(id != -1){
                lands.add(new Land(new LandData(id,title,color,new ArrayList<>(),new ArrayList<>())));
            }else {
                lands.add(new Land(new LandData(false,title,color,new ArrayList<>(),new ArrayList<>())));
            }
        }
    }
    private static void getLandZoneData(Sheet sheet, List<LandZone> zones) {
        long id, lid;
        String title, points;
        ColorData color;
        for (Row row : sheet) {
            id = -1;
            lid = -1;
            title = "";
            color = null;
            points = "";//FIXME:BORDER -> POINTS

            for (Cell cell : row) {
                switch (cell.getColumnIndex()){
                    case 0:
                        if(cell.getCellType() == CellType.NUMERIC)
                            id = Double.valueOf(cell.getNumericCellValue()).longValue();
                        break;
                    case 1:
                        if(cell.getCellType() == CellType.NUMERIC)
                            lid = Double.valueOf(cell.getNumericCellValue()).longValue();
                        break;
                    case 2:
                        title = cell.getStringCellValue().trim();
                        break;
                    case 3:
                        color = new ColorData(cell.getStringCellValue().trim());
                        break;
                    case 4:
                        points = cell.getStringCellValue().trim();
                        break;
                }
            }

            if(id != -1){
                zones.add(new LandZone(new LandZoneData(id,lid,title,color,new ArrayList<>())));
            }else{
                zones.add(new LandZone(new LandZoneData(lid,title,color,new ArrayList<>())));
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
        String username, email, phone;
        for (Row row : sheet) {
            id = -1;
            username = "";
            email = "";
            phone = "";

            for (Cell cell : row) {
                switch (cell.getColumnIndex()){
                    case 0:
                        if(cell.getCellType() == CellType.NUMERIC)
                            id = Double.valueOf(cell.getNumericCellValue()).longValue();
                        break;
                    case 1:
                        username = cell.getStringCellValue().trim();
                        break;
                    case 2:
                        email = cell.getStringCellValue().trim();
                        break;
                    case 3:
                        phone = cell.getStringCellValue().trim();
                        break;
                }
            }

            if (id != -1) {
                contacts.add(new Contact(id, username, email, phone));
            }else{
                contacts.add(new Contact(username, email, phone));
            }
        }
    }

    protected static void processLandsSheet(
            ReadOnlySharedStringsTable strings,
            InputStream sheetInputStream,
            List<Land> lands
    ) throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id;
                    private String title, color, points;
                    @Override
                    public void startRow(int rowNum) {
                        id = -1;
                        title = "";
                        color = "";
                        points = "";
                    }

                    @Override
                    public void endRow(int rowNum) {
                        Land land;
                        try{
                            List<LatLng> border = new ArrayList<>();
                            List<List<LatLng>> holes = new ArrayList<>();
                            //fixme: points -> border && holes
                            if(id != -1)
                                land = new Land(new LandData(id,title,new ColorData(color),border,holes));
                            else
                                land = new Land(new LandData(false,title,new ColorData(color),border,holes));
                        }catch (Exception e){
                            land = null;
                        }
                        if(land != null)
                            lands.add(land);
                    }

                    @Override
                    public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                id = Long.parseLong(value);
                                break;
                            case "B":
                                title = value;
                                break;
                            case "C":
                                color = value;
                                break;
                            case "D":
                                points = value;
                                break;
                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
    protected static void processZonesSheet(
            ReadOnlySharedStringsTable strings,
            InputStream sheetInputStream,
            List<LandZone> zones
    ) throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id, lid;
                    private String title,color,points;
                    @Override
                    public void startRow(int rowNum) {
                        id = -1;
                        lid = -1;
                        title = "";
                        color = "";
                        points = "";
                    }

                    @Override
                    public void endRow(int rowNum) {
                        LandZone zone;
                        try {
                            List<LatLng> border = new ArrayList<>();
                            //fixme: points -> border
                            if(id != -1)
                                zone = new LandZone(new LandZoneData(id,lid,title,new ColorData(color),border));
                            else
                                zone = new LandZone(new LandZoneData(lid,title,new ColorData(color),border));
                        }catch (Exception e){
                            zone = null;
                        }
                        if(zone != null)
                            zones.add(zone);
                    }

                    @Override
                    public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                id = Long.parseLong(value);
                                break;
                            case "B":
                                lid = Long.parseLong(value);
                                break;
                            case "C":
                                title = value;
                                break;
                            case "D":
                                color = value;
                                break;
                            case "E":
                                points = value;
                                break;

                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
    protected static void processRecordsSheet(
            ReadOnlySharedStringsTable strings,
            InputStream sheetInputStream,
            List<LandDataRecord> records
    ) throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id,lid;
                    private String title,color,action,date,points;
                    @Override
                    public void startRow(int rowNum) {
                        id = -1;
                        lid = -1;
                        title = "";
                        color = "";
                        action = "";
                        date = "";
                        points = "";
                    }

                    @Override
                    public void endRow(int rowNum) {
                        //Log.d(TAG, "Record "+id+": "+lid+" "+title+" "+color+" "+action+" "+date+" "+points);
                    }

                    @Override
                    public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                id = Long.parseLong(value);
                                break;
                            case "B":
                                lid = Long.parseLong(value);
                                break;
                            case "C":
                                title = value;
                                break;
                            case "D":
                                color = value;
                                break;
                            case "E":
                                action = value;
                                break;
                            case "F":
                                date = value;
                                break;
                            case "G":
                                points = value;
                                break;
                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
    protected static void processContactsSheet(
            ReadOnlySharedStringsTable strings,
            InputStream sheetInputStream,
            List<Contact> contacts
    ) throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id;
                    private String username, email, phone;
                    @Override
                    public void startRow(int rowNum) {
                        id = -1;
                        username = "";
                        email = "";
                        phone = "";
                    }

                    @Override
                    public void endRow(int rowNum) {
                        Contact contact;
                        try{
                            if(id != -1)
                                contact = new Contact(id,username,email,phone);
                            else
                                contact = new Contact(username,email,phone);
                        }catch (Exception e){
                            contact = null;
                        }
                        if(contact != null)
                            contacts.add(contact);
                    }

                    @Override
                    public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                id = Long.parseLong(value);
                                break;
                            case "B":
                                username = value;
                                break;
                            case "C":
                                email = value;
                                break;
                            case "D":
                                phone = value;
                                break;
                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
}
