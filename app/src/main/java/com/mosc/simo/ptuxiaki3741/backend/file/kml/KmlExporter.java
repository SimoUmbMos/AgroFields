package com.mosc.simo.ptuxiaki3741.backend.file.kml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.models.Land;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class KmlExporter {
    public static Document kmlFileExporter(List<Land> landList) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        if(landList.size() == 0) return doc;
        Element kmlTag = doc.createElement("kml");
        kmlTag.setAttribute("xmlns","http://www.opengis.net/kml/2.2");
        kmlTag.setAttribute("xmlns:kml","http://www.opengis.net/kml/2.2");
        kmlTag.setAttribute("xmlns:gx","http://www.google.com/kml/ext/2.2");
        kmlTag.setAttribute("xmlns:atom","http://www.w3.org/2005/Atom");

        Element documentTag = doc.createElement("Document");

        Element nameTag = doc.createElement("name");
        nameTag.appendChild(doc.createTextNode("Lands"));
        documentTag.appendChild(nameTag);

        Element placemarkTag, placemarkNameTag;
        Element polygonTag, extrudeTag, altitudeModeTag;

        Element outerBoundaryIsTag, innerBoundaryIsTag;
        Element LinearRingTag, coordinatesTag;
        StringBuilder coordinatesValues = new StringBuilder();
        LatLng temp;
        for(Land land:landList){
            if(land.getData() == null) continue;
            if(land.getData().getBorder().size() == 0) continue;

            placemarkTag = doc.createElement("Placemark");

            placemarkNameTag = doc.createElement("name");
            placemarkNameTag.appendChild(doc.createTextNode("Land"));
            placemarkTag.appendChild(placemarkNameTag);

            polygonTag = doc.createElement("Polygon");

            extrudeTag = doc.createElement("extrude");
            extrudeTag.appendChild(doc.createTextNode("1"));
            polygonTag.appendChild(extrudeTag);

            altitudeModeTag = doc.createElement("altitudeMode");
            altitudeModeTag.appendChild(doc.createTextNode("relativeToGround"));
            polygonTag.appendChild(altitudeModeTag);

            outerBoundaryIsTag = doc.createElement("outerBoundaryIs");
            LinearRingTag = doc.createElement("LinearRing");
            coordinatesTag = doc.createElement("coordinates");
            coordinatesValues.setLength(0);
            for(int i = 0; i < land.getData().getBorder().size(); i++){
                temp = land.getData().getBorder().get(i);
                if(i != 0)
                    coordinatesValues.append(" ");
                coordinatesValues
                        .append(temp.longitude)
                        .append(",")
                        .append(temp.latitude)
                        .append(",")
                        .append(0);
            }
            coordinatesTag.appendChild(doc.createTextNode(coordinatesValues.toString()));
            LinearRingTag.appendChild(coordinatesTag);
            outerBoundaryIsTag.appendChild(LinearRingTag);
            polygonTag.appendChild(outerBoundaryIsTag);

            if(land.getData().getHoles().size() != 0){
                innerBoundaryIsTag = doc.createElement("innerBoundaryIs");
                for(List<LatLng> hole : land.getData().getHoles()){
                    LinearRingTag = doc.createElement("LinearRing");

                    coordinatesTag = doc.createElement("coordinates");
                    coordinatesValues.setLength(0);
                    for(int i = 0; i < hole.size(); i++){
                        temp = hole.get(i);
                        if(i != 0)
                            coordinatesValues.append(" ");
                        coordinatesValues
                                .append(temp.longitude)
                                .append(",")
                                .append(temp.latitude)
                                .append(",")
                                .append(0);
                    }
                    coordinatesTag.appendChild(doc.createTextNode(coordinatesValues.toString()));
                    LinearRingTag.appendChild(coordinatesTag);
                    innerBoundaryIsTag.appendChild(LinearRingTag);
                }
                polygonTag.appendChild(innerBoundaryIsTag);
            }

            placemarkTag.appendChild(polygonTag);
            documentTag.appendChild(placemarkTag);
        }
        kmlTag.appendChild(documentTag);
        doc.appendChild(kmlTag);
        return doc;
    }
}
