package com.mosc.simo.ptuxiaki3741.backend.file.openxml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public final class OpenXmlDataBaseInput {
    private static final String TAG = "OpenXmlDataBaseInput";
    private OpenXmlDataBaseInput(){}
    public static boolean importDB(InputStream is, List<Land> lands, List<LandZone> zones){
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
            return false;
        }

        try{
            Workbook workbook = WorkbookFactory.create(is1);
            lands.clear();
            zones.clear();
            readDataFromWorkbook(workbook,lands,zones);
            return true;
        }catch (Exception e) {
            Log.w(TAG, "importDB: ", e);
        }

        try{
            OPCPackage container;
            String sheetName;
            container = OPCPackage.open(is2);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
            XSSFReader xssfReader = new XSSFReader(container);
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            lands.clear();
            zones.clear();
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
                }

                stream.close();
            }
            return true;
        }catch (Exception e){
            Log.w(TAG, "importDB: ", e);
        }

        return false;
    }

    private static void readDataFromWorkbook(
            Workbook workbook,
            List<Land> lands,
            List<LandZone> zones
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
            }
        }
        workbook.close();
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
            title = "";
            tags = "";
            color = null;
            snapshot = -1;
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
                        }
                        break;
                    case 1:
                        boolean isDouble;
                        try{
                            snapshot = Long.parseLong(cell.getStringCellValue().trim());
                            isDouble = false;
                        }catch (Exception ignore){
                            isDouble = true;
                        }
                        if(isDouble){
                            try{
                                snapshot = (long) Double.parseDouble(cell.getStringCellValue().trim());
                            }catch (Exception ignore){}
                        }
                        break;
                    case 2:
                        title = cell.getStringCellValue().trim();
                        break;
                    case 3:
                        tags = cell.getStringCellValue().trim();
                        break;
                    case 4:
                        color = new ColorData(cell.getStringCellValue().trim());
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
        long snapshot;
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
            snapshot = -1;
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
                        }
                        break;
                    case 1:
                        boolean isDouble;
                        try{
                            snapshot = Long.parseLong(cell.getStringCellValue().trim());
                            isDouble = false;
                        }catch (Exception ignore){
                            isDouble = true;
                        }
                        if(isDouble){
                            try{
                                snapshot = (long) Double.parseDouble(cell.getStringCellValue().trim());
                            }catch (Exception ignore){}
                        }
                        break;
                    case 2:
                        if(cell.getCellType() == CellType.NUMERIC){
                            try{
                                lid = Double.valueOf(cell.getNumericCellValue()).longValue();
                            }catch (Exception e){
                                lid = -1;
                            }
                        }
                        break;
                    case 3:
                        title = cell.getStringCellValue().trim();
                        break;
                    case 4:
                        note = cell.getStringCellValue().trim();
                        break;
                    case 5:
                        tags = cell.getStringCellValue().trim();
                        break;
                    case 6:
                        color = new ColorData(cell.getStringCellValue().trim());
                        break;
                    case 7:
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

            zones.add(new LandZone(new LandZoneData(id,snapshot,lid,title,note,tags,color,border)));
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
                    private long snapshot;
                    private String title, tags, color;
                    private List<LatLng> border;
                    private List<List<LatLng>> holes;
                    @Override
                    public void startRow(int rowNum) {
                        id = 0;
                        title = "";
                        tags = "";
                        color = "";
                        snapshot = -1;
                        border = new ArrayList<>();
                        holes = new ArrayList<>();
                    }

                    @Override
                    public void endRow(int rowNum) {
                        if(!title.isEmpty() && border.size() != 0) {
                            lands.add(new Land(new LandData(id, snapshot, title, tags, new ColorData(color), border, holes)));
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
                                try{
                                    id = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    id = 0;
                                }
                                break;
                            case "B":
                                boolean isDouble;
                                try{
                                    snapshot = Long.parseLong(value.trim());
                                    isDouble = false;
                                }catch (Exception ignore){
                                    isDouble = true;
                                }
                                if(isDouble){
                                    try{
                                        snapshot = (long) Double.parseDouble(value.trim());
                                    }catch (Exception ignore){}
                                }
                                break;
                            case "C":
                                title = value;
                                break;
                            case "D":
                                tags = value;
                                break;
                            case "E":
                                color = value;
                                break;
                            case "F":
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
                    private long snapshot;
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
                        snapshot = -1;
                        border = new ArrayList<>();
                    }

                    @Override
                    public void endRow(int rowNum) {
                        if(lid != -1 && !title.isEmpty() && border.size() != 0){
                            zones.add(new LandZone(new LandZoneData(id,snapshot,lid,title,note,tags,new ColorData(color),border)));
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
                                try{
                                    id = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    id = 0;
                                }
                                break;
                            case "B":
                                boolean isDouble;
                                try{
                                    snapshot = Long.parseLong(value.trim());
                                    isDouble = false;
                                }catch (Exception ignore){
                                    isDouble = true;
                                }
                                if(isDouble){
                                    try{
                                        snapshot = (long) Double.parseDouble(value.trim());
                                    }catch (Exception ignore){}
                                }
                                break;
                            case "C":
                                try{
                                    lid = Double.valueOf(value).longValue();
                                }catch (Exception e){
                                    lid = -1;
                                }
                                break;
                            case "D":
                                title = value;
                                break;
                            case "E":
                                note = value;
                                break;
                            case "F":
                                tags = value;
                                break;
                            case "G":
                                color = value;
                                break;
                            case "H":
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
