package com.mosc.simo.ptuxiaki3741.backend.file.kml;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class KmlReader {
    public static ArrayList<LandData> exec(Context context, InputStream input) throws ParserConfigurationException, IOException, SAXException {
        ArrayList<LandData> result = new ArrayList<>();
        if(input == null) return result;
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(input);
        NodeList polygons = document.getElementsByTagName("Polygon");
        for(int i = 0; i < polygons.getLength(); i++){
            if(polygons.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            Element polygon = (Element) polygons.item(i);
            LandData temp = readPolygon(context, polygon);
            if(temp.getBorder().size() != 0)
                result.add(temp);
        }
        return result;
    }
    private static LandData readPolygon(Context context, Element polygon){
        return new LandData(
                DataUtil.getRandomLandColor(context),
                readBorder(polygon),
                readHoles(polygon)
        );
    }
    private static List<LatLng> readBorder(Element polygon){
        NodeList outerBoundaryIsList = polygon.getElementsByTagName("outerBoundaryIs");
        if(outerBoundaryIsList.getLength() == 0) return new ArrayList<>();
        if(outerBoundaryIsList.item(0).getNodeType() != Node.ELEMENT_NODE) return new ArrayList<>();
        Element outerBoundaryIs = (Element) outerBoundaryIsList.item(0);
        List<List<LatLng>> result = readCoordinates(outerBoundaryIs);
        if(result.size() > 0) return result.get(0);
        return new ArrayList<>();
    }
    private static List<List<LatLng>> readHoles(Element polygon){
        NodeList innerBoundaryIsList = polygon.getElementsByTagName("innerBoundaryIs");
        if(innerBoundaryIsList.getLength() == 0) return new ArrayList<>();
        if(innerBoundaryIsList.item(0).getNodeType() != Node.ELEMENT_NODE) return new ArrayList<>();
        Element innerBoundaryIs = (Element) innerBoundaryIsList.item(0);
        return readCoordinates(innerBoundaryIs);
    }
    private static List<List<LatLng>> readCoordinates(Element root){
        List<List<LatLng>> result = new ArrayList<>();
        List<LatLng> temp = new ArrayList<>();
        NodeList coordinatesList = root.getElementsByTagName("coordinates");
        for(int i = 0; i < coordinatesList.getLength(); i++){
            temp.clear();
            String[] coordinatePairsList = coordinatesList.item(i)
                    .getFirstChild().getNodeValue()
                    .trim()
                    .replaceAll("\n"," ")
                    .replaceAll("\t"," ")
                    .replaceAll(" +", " ")
                    .split(" ");
            for(String coordinatePairs : coordinatePairsList){
                String[] coordinates = coordinatePairs.split(",");
                if(coordinates.length < 2) continue;
                temp.add(new LatLng(
                        Double.parseDouble(coordinates[1]),
                        Double.parseDouble(coordinates[0])
                ));
            }
            if(temp.size()>0) result.add(temp);
        }
        return result;
    }
}
