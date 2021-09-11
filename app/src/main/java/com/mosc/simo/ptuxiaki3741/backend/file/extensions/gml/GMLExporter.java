package com.mosc.simo.ptuxiaki3741.backend.file.extensions.gml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

public class GMLExporter {
    public static Document exportList(List<Land> lands){
        Document doc = new Document();
        int size = 0;
        Element root = createRootElement();
        Element featureMember = createElement("featureMember");
        Element multiPolygon = createMultiPolygonElement();
        Element polygon;
        for(Land land:lands){
            polygon = createElementFromLand(land);
            if(polygon != null){
                multiPolygon.addContent(polygon);
                size++;
            }
        }
        if(size > 0){
            featureMember.addContent(multiPolygon);
        }
        root.addContent(featureMember);
        doc.addContent(root);
        return doc;
    }
    private static Element createRootElement(){
        Namespace ns2 = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs");
        Namespace ns3 = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        Namespace ns4 = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace ns5 = Namespace.getNamespace("schemaLocation", "http://ogr.maptools.org/ http://ogr.maptools.org/lineogr.xsd");

        Element element = new Element("FeatureCollection",ns2);
        element.setNamespace(ns2);
        element.addNamespaceDeclaration(ns2);
        element.addNamespaceDeclaration(ns3);
        element.addNamespaceDeclaration(ns4);
        element.addNamespaceDeclaration(ns5);
        return element;
    }
    private static Element createElement(String elementTag){
        Namespace ns = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        return new Element(elementTag,ns);
    }
    private static Element createMultiPolygonElement(){
        Namespace ns = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        Element element = new Element("MultiPolygon",ns);
        element.setAttribute("srsName","http://www.opengis.net/gml/srs/epsg.xml#4326");
        return element;
    }
    private static Element createElementCoordinates(List<LatLng> borders){
        Element root = createElement("coordinates");
        Namespace ns = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        root.setNamespace(ns);
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
        root.addContent(coordinatesString.toString());
        return root;
    }

    private static Element createElementFromLand(Land land){
        Element root = createElement("polygonMember");
        Element polygon = createElement("Polygon");
        Element outerBoundary = createElement("outerBoundaryIs");
        Element linearRing = createElement("LinearRing");
        if(land.getData().getBorder() != null){
            if(land.getData().getBorder().size()>0){
                linearRing.addContent(createElementCoordinates(land.getData().getBorder()));
                outerBoundary.addContent(linearRing);
                polygon.addContent(outerBoundary);
                root.addContent(polygon);
                return root;
            }
        }
        return null;
    }
}
