package com.mosc.simo.ptuxiaki3741.backend.file.kml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class KmlReader {
    public static ArrayList<LandData> exec(InputStream is) throws Exception{
        ArrayList<LandData> border_fragment = new ArrayList<>();
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(is);

        NodeList polygonTags;
        NodeList outerBoundaryTags;
        NodeList innerBoundaryTags;

        NodeList coordList;

        Element polygon;
        Element outerBoundary;
        Element innerBoundary;

        List<List<LatLng>> holes = new ArrayList<>();
        List<LatLng> hole = new ArrayList<>();
        List<LatLng> border = new ArrayList<>();

        if (document == null) return border_fragment;

        polygonTags = document.getElementsByTagName("Polygon");
        for(int i = 0; i < polygonTags.getLength(); i++){
            if (polygonTags.item(i).getNodeType() == Node.ELEMENT_NODE) {
                polygon = (Element) polygonTags.item(i);

                border.clear();
                holes.clear();

                outerBoundaryTags = polygon.getElementsByTagName("outerBoundaryIs");
                for(int j = 0; j < outerBoundaryTags.getLength(); j++){
                    if (outerBoundaryTags.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        outerBoundary = (Element) outerBoundaryTags.item(j);

                        coordList = outerBoundary.getElementsByTagName("coordinates");
                        for (int z = 0; z < coordList.getLength(); z++) {
                            String[] coordinatePairs = coordList.item(z).getFirstChild()
                                    .getNodeValue().trim().split(" ");
                            for (String coord : coordinatePairs) {
                                border.add(new LatLng(Double.parseDouble(coord.split(",")[1]),
                                        Double.parseDouble(coord.split(",")[0])));
                            }
                        }

                    }
                }

                innerBoundaryTags = polygon.getElementsByTagName("innerBoundaryIs");
                for(int j = 0; j < innerBoundaryTags.getLength(); j++){
                    if (innerBoundaryTags.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        innerBoundary = (Element) innerBoundaryTags.item(j);

                        coordList = innerBoundary.getElementsByTagName("coordinates");
                        for (int z = 0; z < coordList.getLength(); z++) {
                            String[] coordinatePairs = coordList.item(z).getFirstChild()
                                    .getNodeValue().trim().split(" ");

                            hole.clear();
                            for (String coord : coordinatePairs) {
                                hole.add(new LatLng(Double.parseDouble(coord.split(",")[1]),
                                        Double.parseDouble(coord.split(",")[0])));
                            }
                            holes.add(new ArrayList<>(hole));
                        }
                    }
                }

                border_fragment.add(new LandData(border,holes));
            }
        }
        return border_fragment;
    }
}
