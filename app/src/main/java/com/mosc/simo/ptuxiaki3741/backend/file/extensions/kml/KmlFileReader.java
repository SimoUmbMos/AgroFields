package com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

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


public class KmlFileReader {
    public static List<Land> exec(InputStream is){
        List<Land> border_fragment = new ArrayList<>();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(is);
            NodeList polygonTags, outerBoundaryTags, coordList;
            Element polygon,outerBoundary;
            List<LatLng> positions = new ArrayList<>();

            if (document == null) return border_fragment;

            //get polygon
            polygonTags = document.getElementsByTagName("Polygon");
            for(int i = 0; i < polygonTags.getLength(); i++){
                if (polygonTags.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    polygon = (Element) polygonTags.item(i);
                    outerBoundaryTags = polygon.getElementsByTagName("outerBoundaryIs");

                    for(int j = 0; j < outerBoundaryTags.getLength(); j++){
                        if (outerBoundaryTags.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            outerBoundary = (Element) outerBoundaryTags.item(j);

                            coordList = outerBoundary.getElementsByTagName("coordinates");
                            for (int z = 0; z < coordList.getLength(); z++) {
                                String[] coordinatePairs = coordList.item(z).getFirstChild()
                                        .getNodeValue().trim().split(" ");
                                positions.clear();
                                for (String coord : coordinatePairs) {
                                    positions.add(new LatLng(Double.parseDouble(coord.split(",")[1]),
                                            Double.parseDouble(coord.split(",")[0])));
                                }
                                border_fragment.add(new Land(new LandData(positions)));
                            }

                        }
                    }

                }
            }


        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return border_fragment;
    }
}
