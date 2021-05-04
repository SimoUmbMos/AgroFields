package com.mosc.simo.ptuxiaki3741.util.file.extensions.kml.async;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//https://github.com/narru19/KMLParser
public class KmlParserTask implements Callable<List<List<LatLng>>> {
    private final InputStream inputStream;
    private final String input;
    public KmlParserTask(InputStream inputStream){
        this.inputStream=inputStream;
        this.input="";
    }
    public KmlParserTask(String input){
        this.inputStream=null;
        this.input=input;
    }

    @Override
     public List<List<LatLng>>call(){
        List<List<LatLng>> border_fragment = new ArrayList<>();
         try {
             DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document document;
             if(inputStream != null){
                 document = docBuilder.parse(inputStream);
             }else{
                 document = docBuilder.parse(input);
             }
             NodeList coordList;

             if (document == null) return null;

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
