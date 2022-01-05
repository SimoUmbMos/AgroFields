package com.mosc.simo.ptuxiaki3741.file.kml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

public class KmlExporter {
    public static Document kmlFileExporter(String key,List<Land> lands){
        Document doc = new Document();
        try{
            Element root = createElement("kml");
            Element document = createElement("Document");
            Element folder = createElement("Folder");
            document.addContent(createSchemaHeader(key));
            folder.addContent(createFolderHeader(key));
            Element placeMark;
            int i = 0;
            for(Land land : lands){
                placeMark = createPlaceMarkHeader(key, land, i+1);
                placeMark.addContent(createGeometry(land.getData()));
                folder.addContent(placeMark);
                i++;
            }
            document.addContent(folder);
            root.addContent(document);
            doc.addContent(root);
        }catch (Exception e){
            e.printStackTrace();
        }
        return doc;
    }

    private static Element createGeometry(LandData data) {
        Element multiGeometry = createElement("MultiGeometry"),
                polygon = createElement("Polygon"),
                outerBoundaryIs, innerBoundaryIs, linearRing, coordinates;
        StringBuilder coordinatesString = new StringBuilder();
        int holesNumber = 0;

        outerBoundaryIs = createElement("outerBoundaryIs");
        linearRing = createElement("LinearRing");
        coordinates = createElement("coordinates");
        for(LatLng point : data.getBorder()){
            coordinatesString
                    .append(point.longitude)
                    .append(",")
                    .append(point.latitude)
                    .append(" ");
        }
        if(data.getBorder().size()>0){
            coordinatesString
                    .append(data.getBorder().get(0).longitude)
                    .append(",")
                    .append(data.getBorder().get(0).latitude)
                    .append(" ");
        }
        coordinates.addContent(coordinatesString.toString());
        linearRing.addContent(coordinates);
        outerBoundaryIs.addContent(linearRing);

        innerBoundaryIs = createElement("innerBoundaryIs");
        for(List<LatLng> hole : data.getHoles()){
            linearRing = createElement("LinearRing");
            coordinates = createElement("coordinates");
            coordinatesString.setLength(0);
            for(LatLng point : hole){
                coordinatesString
                        .append(point.longitude)
                        .append(",")
                        .append(point.latitude)
                        .append(" ");
            }
            if(hole.size()>0){
                coordinatesString
                        .append(hole.get(0).longitude)
                        .append(",")
                        .append(hole.get(0).latitude)
                        .append(" ");
            }
            coordinates.addContent(coordinatesString.toString());
            linearRing.addContent(coordinates);
            innerBoundaryIs.addContent(linearRing);
            holesNumber++;
        }

        polygon.addContent(outerBoundaryIs);
        if(holesNumber != 0)
            polygon.addContent(innerBoundaryIs);
        multiGeometry.addContent(polygon);
        return multiGeometry;
    }

    private static Element createElement(String elementTag){
        Element element = new Element(elementTag);
        Namespace ns1 = Namespace.getNamespace("http://www.opengis.net/kml/2.2");
        Namespace ns2 = Namespace.getNamespace("ns2", "http://www.google.com/kml/ext/2.2");
        Namespace ns3 = Namespace.getNamespace("ns3", "http://www.w3.org/2005/Atom");
        Namespace ns4 = Namespace.getNamespace("ns4", "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0");
        element.setNamespace(ns1);
        element.addNamespaceDeclaration(ns2);
        element.addNamespaceDeclaration(ns3);
        element.addNamespaceDeclaration(ns4);
        return element;
    }

    private static Element createFolderHeader(String key) {
        Element name = createElement("name");
        name.addContent(key);
        return name;
    }
    private static Element createSchemaHeader(String key) {
        Element schema = createElement("Schema");
        Element simpleField = createElement("SimpleField");
        schema.setAttribute("name",key+"_1");
        schema.setAttribute("id",key+"_1");
        simpleField.setAttribute("type","string");
        simpleField.setAttribute("name","PER");
        schema.addContent(simpleField);
        return schema;
    }
    private static Element createPlaceMarkHeader(String key, Land land, int i){
        Element placeMark;
        Element extendedData;
        Element schemaData;
        Element simpleData;
        placeMark = createElement("Placemark");
        placeMark.setAttribute("id",key+"."+i);
        extendedData = createElement("ExtendedData");
        schemaData = createElement("SchemaData");
        schemaData.setAttribute("schemaUrl","#"+key+"_1");
        simpleData = createElement("SimpleData");
        simpleData.setAttribute("name","PER");
        simpleData.addContent(land.getData().getTitle());
        schemaData.addContent(simpleData);
        extendedData.addContent(schemaData);
        placeMark.addContent(extendedData);
        return placeMark;
    }
}
