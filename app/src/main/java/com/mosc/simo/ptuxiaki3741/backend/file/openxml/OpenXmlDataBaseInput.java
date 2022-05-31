package com.mosc.simo.ptuxiaki3741.backend.file.openxml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarCategory;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.CalendarNotification;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public final class OpenXmlDataBaseInput {
    private static final String TAG = "OpenXmlDataBaseInput";
    private OpenXmlDataBaseInput(){}
    public static OpenXmlState importDB(
            InputStream is
    ){
        InputStream is1;
        InputStream is2;
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            is1 = new ByteArrayInputStream(baos.toByteArray());
            is2 = new ByteArrayInputStream(baos.toByteArray());
        }catch (Exception e){
            return null;
        }

        try{
            Workbook workbook = WorkbookFactory.create(is1);
            return readDataFromWorkbook(workbook);
        }catch (Exception e) {
            Log.w(TAG, "importDB: ", e);
        }

        try{
            OPCPackage container = OPCPackage.open(is2);
            return readDataFromOPCPackage(container);
        }catch (Exception e){
            Log.w(TAG, "importDB: ", e);
        }

        return null;
    }

    private static OpenXmlState readDataFromWorkbook(Workbook workbook)
            throws IOException {
        OpenXmlState state = new OpenXmlState();
        for(Sheet sheet : workbook){
            String sheetName = sheet.getSheetName().toLowerCase().trim();
            if(sheetName.equals(AppValues.sheetLandName.toLowerCase())){
                getLandData(sheet, state.getLands());
            }else if(sheetName.equals(AppValues.sheetLandZoneName.toLowerCase())){
                getLandZoneData(sheet, state.getZones());
            }else if(sheetName.equals(AppValues.sheetCategoriesName.toLowerCase())){
                getCategoryData(sheet, state.getCategories());
            }else if(sheetName.equals(AppValues.sheetNotificationsName.toLowerCase())){
                getNotificationData(sheet, state.getNotifications());
            }
        }
        workbook.close();
        return state;
    }
    private static void getLandData(Sheet sheet, List<Land> lands) {
        long id;
        long snapshot;
        String title;
        String tags;
        ColorData color;
        List<LatLng> border;
        List<List<LatLng>> holes;
        for (Row row : sheet) {
            id = 0;
            snapshot = LocalDate.now().getYear();
            title = "";
            tags = "";
            color = null;
            border = new ArrayList<>();
            holes = new ArrayList<>();

            for (Cell cell : row) {
                switch (cell.getColumnIndex()){
                    case 0:
                        if(cell.getCellType() == CellType.NUMERIC){
                            try{
                                id = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }catch (Exception e){
                                id = 0;
                            }
                        }else if(cell.getCellType() == CellType.STRING){
                            try{
                                id = Double.valueOf(cell.getStringCellValue()).longValue();
                            }catch (Exception e){
                                id = 0;
                            }
                        }
                        break;
                    case 1:
                        if(cell.getCellType() == CellType.NUMERIC){
                            try{
                                snapshot = (long) cell.getNumericCellValue();
                            }catch (Exception e){
                                snapshot = LocalDate.now().getYear();
                            }
                        }else if(cell.getCellType() == CellType.STRING){
                            boolean isDouble;
                            try{
                                snapshot = Long.parseLong(cell.getStringCellValue().trim());
                                isDouble = false;
                            }catch (Exception e){
                                isDouble = true;
                            }
                            if(isDouble){
                                try{
                                    snapshot = (long) Double.parseDouble(cell.getStringCellValue().trim());
                                }catch (Exception e){
                                    snapshot = LocalDate.now().getYear();
                                }
                            }
                        }
                        break;
                    case 2:
                        try{
                            title = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            title = "";
                        }
                        break;
                    case 3:
                        try{
                            tags = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            tags = "";
                        }
                        break;
                    case 4:
                        try{
                            color = new ColorData(cell.getStringCellValue().trim());
                        }catch (Exception e){
                            color = AppValues.defaultLandColor;
                        }
                        break;
                    case 5:
                        fillListsFromPointsString(
                                border,
                                holes,
                                cell.getStringCellValue().trim()
                        );
                        break;
                }
            }
            if(title.isEmpty() && border.size() == 0){
                continue;
            }

            lands.add(new Land(new LandData(id,snapshot,title,tags,color,border,holes)));
        }
    }
    private static void getLandZoneData(Sheet sheet, List<LandZone> zones) {
        long id, lid;
        String title, note, tags;
        ColorData color;
        List<LatLng> border;
        for (Row row : sheet) {
            id = 0;
            lid = -1;
            title = "";
            note = "";
            tags = "";
            color = null;
            border = new ArrayList<>();

            for (Cell cell : row) {
                switch (cell.getColumnIndex()){
                    case 0:
                        if(cell.getCellType() == CellType.NUMERIC){
                            try{
                                id = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }catch (Exception e){
                                id = 0;
                            }
                        }else if(cell.getCellType() == CellType.STRING){
                            try{
                                id = Double.valueOf(cell.getStringCellValue()).longValue();
                            }catch (Exception e){
                                id = 0;
                            }
                        }
                        break;
                    case 1:
                        if(cell.getCellType() == CellType.NUMERIC){
                            try{
                                lid = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }catch (Exception e){
                                lid = -1;
                            }
                        }else if(cell.getCellType() == CellType.STRING){
                            try{
                                lid = Double.valueOf(cell.getStringCellValue()).longValue();
                            }catch (Exception e){
                                lid = -1;
                            }
                        }
                        break;
                    case 2:
                        try{
                            title = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            title = "";
                        }
                        break;
                    case 3:
                        try{
                            note = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            note = "";
                        }
                        break;
                    case 4:
                        try{
                            tags = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            tags = "";
                        }
                        break;
                    case 5:
                        try{
                            color = new ColorData(cell.getStringCellValue().trim());
                        }catch (Exception e){
                            color = AppValues.defaultZoneColor;
                        }
                        break;
                    case 6:
                        fillListsFromPointsString(
                                border,
                                new ArrayList<>(),
                                cell.getStringCellValue().trim()
                        );
                        break;
                }
            }
            if(lid == -1 && title.isEmpty() && border.size() == 0){
                continue;
            }

            zones.add(new LandZone(new LandZoneData(id,lid,title,note,tags,color,border)));
        }
    }
    private static void getCategoryData(Sheet sheet, List<CalendarCategory> categories){
        long id;
        String name;
        ColorData color;
        for (Row row : sheet) {
            id = 0;
            name = "";
            color = null;
            for (Cell cell : row) {
                switch (cell.getColumnIndex()){
                    case 0:
                        if(cell.getCellType() == CellType.NUMERIC){
                            try{
                                id = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }catch (Exception e){
                                id = 0;
                            }
                        }else if(cell.getCellType() == CellType.STRING){
                            try{
                                id = Double.valueOf(cell.getStringCellValue()).longValue();
                            }catch (Exception e){
                                id = 0;
                            }
                        }
                        break;
                    case 1:
                        try{
                            name = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            name = "";
                        }
                        break;
                    case 2:
                        try{
                            color = new ColorData(cell.getStringCellValue().trim());
                        }catch (Exception e){
                            color = null;
                        }
                        break;
                }
            }
            if(color != null)
                categories.add(new CalendarCategory(id,name,color));
        }
    }
    private static void getNotificationData(Sheet sheet, List<CalendarNotification> notifications){
        long id, categoryID, dateMillis;
        Long snapshot, lid, zid;
        String title, message;
        Date date;

        for (Row row : sheet) {
            id = 0;
            categoryID = AppValues.defaultCalendarCategoryID;
            snapshot = null;
            lid = null;
            zid = null;
            title = "";
            message = "";
            date = null;

            for(Cell cell : row){
                switch (cell.getColumnIndex()){
                    case 0:
                        try{
                            if(cell.getCellType() == CellType.NUMERIC){
                                id = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }else if(cell.getCellType() == CellType.STRING){
                                if(!cell.getStringCellValue().isEmpty()){
                                    id = Double.valueOf(cell.getStringCellValue()).longValue();
                                }else{
                                    id = 0;
                                }
                            }
                        }catch (Exception e){
                            id = 0;
                        }
                        break;
                    case 1:
                        try{
                            if(cell.getCellType() == CellType.NUMERIC){
                                categoryID = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }else if(cell.getCellType() == CellType.STRING){
                                if(!cell.getStringCellValue().isEmpty()){
                                    categoryID = Double.valueOf(cell.getStringCellValue()).longValue();
                                }else{
                                    categoryID = 0;
                                }
                            }
                        }catch (Exception e){
                            categoryID = 0;
                        }
                        break;
                    case 2:
                        try{
                            if(cell.getCellType() == CellType.NUMERIC){
                                snapshot = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }else if(cell.getCellType() == CellType.STRING){
                                if(!cell.getStringCellValue().isEmpty()){
                                    snapshot = Double.valueOf(cell.getStringCellValue()).longValue();
                                }else{
                                    snapshot = null;
                                }
                            }
                        }catch (Exception e){
                            snapshot = null;
                        }
                        break;
                    case 3:
                        try{
                            if(cell.getCellType() == CellType.NUMERIC){
                                lid = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }else if(cell.getCellType() == CellType.STRING){
                                if(!cell.getStringCellValue().isEmpty()){
                                    lid = Double.valueOf(cell.getStringCellValue()).longValue();
                                }else{
                                    lid = null;
                                }
                            }
                        }catch (Exception e){
                            lid = null;
                        }
                        break;
                    case 4:
                        try{
                            if(cell.getCellType() == CellType.NUMERIC){
                                zid = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }else if(cell.getCellType() == CellType.STRING){
                                if(!cell.getStringCellValue().isEmpty()){
                                    zid = Double.valueOf(cell.getStringCellValue()).longValue();
                                }else{
                                    zid = null;
                                }
                            }
                        }catch (Exception e){
                            zid = null;
                        }
                        break;
                    case 5:
                        try{
                            title = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            title = "";
                        }
                        break;
                    case 6:
                        try{
                            message = cell.getStringCellValue().trim();
                        }catch (Exception e){
                            message = "";
                        }
                        break;
                    case 7:
                        try{
                            if(cell.getCellType() == CellType.NUMERIC){
                                dateMillis = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }else if(cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isEmpty()){
                                dateMillis = Double.valueOf(cell.getStringCellValue()).longValue();
                            }else{
                                dateMillis = 0;
                            }
                            if(dateMillis > 0)
                                date = new Date(dateMillis);
                            else
                                date = null;
                        }catch (Exception e){
                            date = null;
                        }
                        break;
                }
            }

            if(snapshot != null && date != null)
                notifications.add(new CalendarNotification(
                        id,
                        categoryID,
                        snapshot,
                        lid,
                        zid,
                        title,
                        message,
                        date
                ));
        }
    }

    private static OpenXmlState readDataFromOPCPackage(OPCPackage container)
            throws IOException, SAXException, OpenXML4JException, ParserConfigurationException {
        OpenXmlState state = new OpenXmlState();
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
        XSSFReader xssfReader = new XSSFReader(container);
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        String sheetName;
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            sheetName = iter.getSheetName().toLowerCase().trim();
            if(sheetName.equals(AppValues.sheetLandName.toLowerCase())){
                getLandData(strings, stream, state.getLands());
            }else if(sheetName.equals(AppValues.sheetLandZoneName.toLowerCase())){
                getLandZoneData(strings, stream, state.getZones());
            }else if(sheetName.equals(AppValues.sheetCategoriesName.toLowerCase())){
                getCategoryData(strings, stream, state.getCategories());
            }else if(sheetName.equals(AppValues.sheetNotificationsName.toLowerCase())){
                getNotificationData(strings, stream, state.getNotifications());
            }
            stream.close();
        }
        return state;
    }
    private static void getLandData(ReadOnlySharedStringsTable strings, InputStream sheetInputStream, List<Land> lands)
            throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id;
                    private long snapshot;
                    private String title, tags, color;
                    private List<LatLng> border;
                    private List<List<LatLng>> holes;
                    @Override
                    public void startRow(int rowNum) {
                        id = 0;
                        snapshot = LocalDate.now().getYear();
                        title = "";
                        tags = "";
                        color = "";
                        border = new ArrayList<>();
                        holes = new ArrayList<>();
                    }

                    @Override
                    public void endRow(int rowNum) {
                        if(!title.isEmpty() && border.size() != 0) {
                            ColorData colorData;
                            try{
                                colorData = new ColorData(color);
                            }catch (Exception e){
                                colorData = AppValues.defaultLandColor;
                            }
                            lands.add(new Land(new LandData(id, snapshot, title, tags, colorData, border, holes)));
                        }
                    }

                    @Override
                    public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    id = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    id = 0;
                                }
                                break;
                            case "B":
                                if(value == null || value.isEmpty()) break;
                                boolean isDouble;
                                try{
                                    snapshot = Long.parseLong(value.trim());
                                    isDouble = false;
                                }catch (Exception e){
                                    isDouble = true;
                                }
                                if(isDouble){
                                    try{
                                        snapshot = (long) Double.parseDouble(value.trim());
                                    }catch (Exception e){
                                        snapshot = LocalDate.now().getYear();
                                    }
                                }
                                break;
                            case "C":
                                if(value == null || value.isEmpty()) break;
                                title = value;
                                break;
                            case "D":
                                if(value == null || value.isEmpty()) break;
                                tags = value;
                                break;
                            case "E":
                                if(value == null || value.isEmpty()) break;
                                color = value;
                                break;
                            case "F":
                                if(value == null || value.isEmpty()) break;
                                fillListsFromPointsString(
                                        border,
                                        holes,
                                        value
                                );
                                break;
                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
    private static void getLandZoneData(ReadOnlySharedStringsTable strings, InputStream sheetInputStream, List<LandZone> zones)
            throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id, lid;
                    private String title,note,tags,color;
                    private List<LatLng> border;
                    @Override
                    public void startRow(int rowNum) {
                        id = 0;
                        lid = -1;
                        title = "";
                        note = "";
                        tags = "";
                        color = "";
                        border = new ArrayList<>();
                    }

                    @Override
                    public void endRow(int rowNum) {
                        if(lid != -1 && !title.isEmpty() && border.size() != 0){
                            ColorData colorData;
                            try{
                                colorData = new ColorData(color);
                            }catch (Exception e){
                                colorData = AppValues.defaultZoneColor;
                            }
                            zones.add(new LandZone(new LandZoneData(id,lid,title,note,tags,colorData,border)));
                        }
                    }

                    @Override
                    public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    id = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    id = 0;
                                }
                                break;
                            case "B":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    lid = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    lid = -1;
                                }
                                break;
                            case "C":
                                if(value == null || value.isEmpty()) break;
                                title = value;
                                break;
                            case "D":
                                if(value == null || value.isEmpty()) break;
                                note = value;
                                break;
                            case "E":
                                if(value == null || value.isEmpty()) break;
                                tags = value;
                                break;
                            case "F":
                                if(value == null || value.isEmpty()) break;
                                color = value;
                                break;
                            case "G":
                                if(value == null || value.isEmpty()) break;
                                fillListsFromPointsString(
                                        border,
                                        new ArrayList<>(),
                                        value
                                );
                                break;

                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
    private static void getCategoryData(ReadOnlySharedStringsTable strings, InputStream sheetInputStream, List<CalendarCategory> categories)
            throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id;
                    private String name, color;
                    @Override public void startRow(int rowNum) {
                        id = 0;
                        name = "";
                        color = "";
                    }
                    @Override public void endRow(int rowNum) {
                        if(color != null && !color.isEmpty()) {
                            ColorData colorData;
                            try{
                                colorData = new ColorData(color);
                            }catch (Exception e){
                                colorData = AppValues.defaultZoneColor;
                            }
                            categories.add(new CalendarCategory(id, name, colorData));
                        }
                    }
                    @Override public void cell(String cellReference, String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();
                        switch (cellLetter){
                            case "A":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    id = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    id = 0;
                                }
                                break;
                            case "B":
                                if(value == null || value.isEmpty()) break;
                                name = value;
                                break;
                            case "C":
                                if(value == null || value.isEmpty()) break;
                                color = value;
                                break;
                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }
    private static void getNotificationData(ReadOnlySharedStringsTable strings, InputStream sheetInputStream, List<CalendarNotification> notifications)
            throws IOException, SAXException, ParserConfigurationException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(
                new StylesTable(),
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private long id;
                    private long categoryID;
                    private Long snapshot, lid, zid;
                    private String title, message;
                    private Date date;
                    @Override public void startRow(int rowNum) {
                        id = 0;
                        categoryID = AppValues.defaultCalendarCategoryID;
                        snapshot = null;
                        lid = null;
                        zid = null;
                        title = "";
                        message = "";
                        date = null;
                    }
                    @Override public void endRow(int rowNum) {
                        if(snapshot != null && date != null) {
                            notifications.add(new CalendarNotification(
                                    id,
                                    categoryID,
                                    snapshot,
                                    lid,
                                    zid,
                                    title,
                                    message,
                                    date
                            ));
                        }
                    }
                    @Override public void cell(String cellReference,final String value, XSSFComment comment) {
                        String cellLetter = cellReference
                                .replaceAll("[0-9]","")
                                .toUpperCase()
                                .trim();

                        switch (cellLetter){
                            case "A":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    id = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    id = 0;
                                }
                                break;
                            case "B":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    categoryID = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    categoryID = AppValues.defaultCalendarCategoryID;
                                }
                                break;
                            case "C":
                                if(value == null || value.isEmpty()) break;
                                boolean isSnapshotDouble;
                                try{
                                    snapshot = Long.parseLong(value.trim());
                                    isSnapshotDouble = false;
                                }catch (Exception e){
                                    isSnapshotDouble = true;
                                }
                                if(isSnapshotDouble){
                                    try{
                                        snapshot = (long) Double.parseDouble(value.trim());
                                    }catch (Exception e){
                                        snapshot = null;
                                    }
                                }
                                break;
                            case "D":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    lid = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    lid = null;
                                }
                                break;
                            case "E":
                                if(value == null || value.isEmpty()) break;
                                try{
                                    zid = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    zid = null;
                                }
                                break;
                            case "F":
                                if(value == null || value.isEmpty()) break;
                                title = value;
                                break;
                            case "G":
                                if(value == null || value.isEmpty()) break;
                                message = value;
                                break;
                            case "H":
                                if(value == null || value.isEmpty()) break;
                                String dateValue = value;
                                if(dateValue.contains(",")){
                                    dateValue = dateValue.replaceAll(",",".");
                                }
                                try{
                                    date = new Date(Double.valueOf(dateValue).longValue());
                                }catch (Exception e){
                                    date = null;
                                }
                                break;
                        }
                    }
                },
                false
        );
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }

    private static void fillListsFromPointsString(List<LatLng> border, List<List<LatLng>> holes, String string){
        List<List<LatLng>> points;
        try{
            points = DataUtil.pointsFromString(string);
        }catch (Exception e){
            points = new ArrayList<>();
        }
        border.clear();
        if(points.size()>0){
            border.addAll(points.get(0));
            points.remove(0);
        }
        holes.clear();
        if (points.size()>0) {
            holes.addAll(points);
        }
    }
}
