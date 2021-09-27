package com.mosc.simo.ptuxiaki3741.file.gml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

public class GMLExporter {
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
        Element featureMember = createElement("featureMember");
        Element geom = createOGRElement();
        Element multiPolygon = createMultiPolygonElement();
        Element polygonMember = createElement("polygonMember");
        Element polygon = createElement("Polygon");
        Element outerBoundary = createElement("outerBoundaryIs");
        Element linearRing = createElement("LinearRing");
        if(land.getData().getBorder() != null){
            if(land.getData().getBorder().size()>0){
                linearRing.addContent(createElementCoordinates(land.getData().getBorder()));
                outerBoundary.addContent(linearRing);
                polygon.addContent(outerBoundary);
                polygonMember.addContent(polygon);
                multiPolygon.addContent(polygonMember);
                geom.addContent(multiPolygon);
                featureMember.addContent(geom);
                return featureMember;
            }
        }
        return null;
    }

    private static Element createRootElement(){
        Namespace ns2 = Namespace.getNamespace("ogr", "http://ogr.maptools.org/");
        Namespace ns3 = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        Namespace ns4 = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace ns5 = Namespace.getNamespace("schemaLocation", "http://ogr.maptools.org/ kml_template.xsd");

        Element element = new Element("FeatureCollection",ns2);
        element.setNamespace(ns2);
        element.addNamespaceDeclaration(ns2);
        element.addNamespaceDeclaration(ns3);
        element.addNamespaceDeclaration(ns4);
        element.addNamespaceDeclaration(ns5);
        return element;
    }
    private static Element createOGRElement(){
        Namespace ns2 = Namespace.getNamespace("ogr", "http://ogr.maptools.org/");

        Element element = new Element("geom",ns2);
        element.setNamespace(ns2);
        return element;
    }
    private static Element createMultiPolygonElement(){
        Namespace ns = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        Element element = new Element("MultiPolygon",ns);
        element.setAttribute("srsName","EPSG:4326");
        return element;
    }
    private static Element createElement(String elementTag){
        Namespace ns = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        return new Element(elementTag,ns);
    }
    private static Element createElementCoordinates(List<LatLng> borders){
        Element root = createElement("coordinates");
        Namespace ns = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        root.setNamespace(ns);
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

}
