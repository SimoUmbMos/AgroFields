package com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
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
    public static List<List<LatLng>> exec(InputStream is) {
        return kmlParse(is);
    }

    public static List<List<LatLng>> kmlParse(InputStream is){
        List<List<LatLng>> border_fragment = new ArrayList<>();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(is);
            NodeList coordList;

            if (document == null) return border_fragment;

            coordList = document.getElementsByTagName("coordinates");

            for (int i = 0; i < coordList.getLength(); i++) {
                String[] coordinatePairs = coordList.item(i).getFirstChild().getNodeValue().trim().split(" ");
                List<LatLng> positions = new ArrayList<>();
                for (String coord : coordinatePairs) {
                    positions.add(new LatLng(Double.parseDouble(coord.split(",")[1]),
                            Double.parseDouble(coord.split(",")[0])));
                }
                border_fragment.add(positions);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return border_fragment;
    }
}
