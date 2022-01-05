package com.mosc.simo.ptuxiaki3741.file.gml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

public class GMLExporter {
    private static final Namespace ns1 = Namespace.getNamespace(
            "gml",
            "http://www.opengis.net/gml"
    );
    private static final Namespace ns2 = Namespace.getNamespace(
            "xsi",
            "http://www.w3.org/2001/XMLSchema-instance"
    );
    private static final Namespace ns3 = Namespace.getNamespace(
            "schemaLocation",
            "http://www.opengis.net/gml http://schemas.opengis.net/gml/2.1.2/feature.xsd"
    );
    private static final Namespace ns4 = Namespace.getNamespace(
            "feature",
            "http://example.com/feature"
    );

    public static Document exportList(List<Land> lands){
        Document doc = new Document();
        Element root = createRootElement();
        Element polygon;
        for(Land land:lands){
            polygon = createElementFromLand(land);
            if(polygon != null){
                root.addContent(polygon);
            }
        }
        doc.addContent(root);
        return doc;
    }

    private static Element createElementFromLand(Land land){
        if(land.getData() != null){
            Element featureMember = createElement("featureMember", ns1);
            Element feature = createElement("feature", ns4);
            Element geometry = createElement("geometry", ns4);
            Element polygon = createElement("Polygon", ns1);
            Element outerBoundaryIs = createElement("outerBoundaryIs", ns1);
            Element linearRing = createElement("LinearRing", ns1);
            linearRing.addContent(createElementCoordinates(land.getData().getBorder()));
            outerBoundaryIs.addContent(linearRing);
            polygon.addContent(outerBoundaryIs);
            geometry.addContent(polygon);
            feature.addContent(geometry);
            featureMember.addContent(feature);
            return featureMember;
        }
        return null;
    }

    private static Element createRootElement(){
        Element element = new Element("FeatureCollection",ns1);
        element.setNamespace(ns1);
        element.addNamespaceDeclaration(ns2);
        element.addNamespaceDeclaration(ns3);
        return element;
    }

    private static Element createElement(String elementTag, Namespace ns){
        Element element = new Element(elementTag,ns);
        element.setNamespace(ns);
        return element;
    }

    private static Element createElementCoordinates(List<LatLng> borders){
        Element root = createElement("coordinates", ns1);
        root.setAttribute("decimal",".");
        root.setAttribute("cs",",");
        root.setAttribute("ts"," ");
        StringBuilder coordinatesString = new StringBuilder();
        for(LatLng point:borders){
            coordinatesString
                    .append(point.longitude)
                    .append(",")
                    .append(point.latitude)
                    .append(" ");
        }
        if(borders.size()>0){
            coordinatesString
                    .append(borders.get(0).longitude)
                    .append(",")
                    .append(borders.get(0).latitude)
                    .append(" ");
        }
        root.addContent(coordinatesString.toString());
        return root;
    }
}