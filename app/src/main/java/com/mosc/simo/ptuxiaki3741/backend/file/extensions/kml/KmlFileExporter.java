package com.mosc.simo.ptuxiaki3741.backend.file.extensions.kml;

import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class KmlFileExporter {
    public static final XMLOutputProcessor XMLOUTPUT = new AbstractXMLOutputProcessor() {
        @Override
        protected void printDeclaration(final Writer out, final FormatStack fstack) throws IOException {
            write(out, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            write(out, fstack.getLineSeparator());
        }
    };

    public static Document kmlFileExporter(String key,List<ExportFieldModel> exportFieldList){
        Document doc = new Document();
        try{
            Element root = createElement("kml");
            Element document = createElement("Document");
            Element folder = createElement("Folder");
            document.addContent(createSchemaHeader(key));
            folder.addContent(createFolderHeader(key));
            Element placeMark;
            for(int i = 0;i<exportFieldList.size();i++){
                placeMark = createPlaceMarkHeader(key,exportFieldList.get(i), i+1);
                placeMark.addContent(createGeometry(exportFieldList.get(i).getPointsList()));
                folder.addContent(placeMark);
            }
            document.addContent(folder);
            root.addContent(document);
            doc.addContent(root);
        }catch (Exception e){
            e.printStackTrace();
        }
        return doc;
    }

    private static Element createGeometry(List<List<List<Double>>> pointsList) {
        Element multiGeometry = createElement("MultiGeometry"),
                polygon, outerBoundaryIs, linearRing, tessellate, coordinates;
        StringBuilder coordinatesString = new StringBuilder();
        for(int i=0;i<pointsList.size();i++){
            coordinatesString.setLength(0);
            polygon = createElement("Polygon");
            outerBoundaryIs = createElement("outerBoundaryIs");
            linearRing = createElement("LinearRing");
            tessellate = createElement("tessellate");
            tessellate.addContent("1");
            coordinates = createElement("coordinates");
            for(int j=0;j<pointsList.get(i).size();j++){
                coordinatesString.append(pointsList.get(i).get(j).get(0)).append(",").append(pointsList.get(i).get(j).get(1)).append(" ");
            }
            coordinates.addContent(coordinatesString.toString());
            linearRing.addContent(tessellate);
            linearRing.addContent(coordinates);
            outerBoundaryIs.addContent(linearRing);
            polygon.addContent(outerBoundaryIs);
            multiGeometry.addContent(polygon);
        }
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
    private static Element createPlaceMarkHeader(String key, ExportFieldModel exportFieldList, int i){
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
        simpleData.addContent(exportFieldList.getTitle());
        schemaData.addContent(simpleData);
        extendedData.addContent(schemaData);
        placeMark.addContent(extendedData);
        return placeMark;
    }
}
