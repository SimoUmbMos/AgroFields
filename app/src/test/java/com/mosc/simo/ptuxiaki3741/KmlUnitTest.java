package com.mosc.simo.ptuxiaki3741;

import com.mosc.simo.ptuxiaki3741.util.file.extensions.kml.KmlFileExporter;
import com.mosc.simo.ptuxiaki3741.util.file.helper.ExportFieldModel;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mosc.simo.ptuxiaki3741.util.file.extensions.kml.KmlFileExporter.XMLOUTPUT;
import static org.junit.Assert.assertEquals;

public class KmlUnitTest {
    @Test
    public void testKmlExport(){
        List<ExportFieldModel> exportFieldModels = new ArrayList<>();
        List<List<List<Double>>> pointsList = new ArrayList<>();
        pointsList.add(debugPoints1());
        pointsList.add(debugPoints2());
        exportFieldModels.add(new ExportFieldModel("Π. ΗΠΕΙΡΟΥ",pointsList));
        Document document = KmlFileExporter.kmlFileExporter("d7f50467-e5ef-49ac-a7ce-15df3e2ed738",exportFieldModels);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat(), XMLOUTPUT);
        System.out.println(xmOut.outputString(document));
        assertEquals(1,1);
    }

    private List<List<Double>> debugPoints1(){
        List<List<Double>> mockFieldPoints = new ArrayList<>();

        List<Double> point = new ArrayList<>();
        point.add(24.098391925087952);point.add(39.342339366569426);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(24.09818482286204);point.add(39.3423665737535);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(24.09805858134959);point.add(39.34229459401947);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(24.098011607674756);point.add(39.34214595589101);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(24.098391925087952);point.add(39.342339366569426);
        mockFieldPoints.add(point);


        List<List<Double>> mockFieldPointsInvert = new ArrayList<>();
        for(int i = (mockFieldPoints.size()-1);i>=0;i--){
            mockFieldPointsInvert.add(mockFieldPoints.get(i));
        }
        return mockFieldPointsInvert;
    }

    private List<List<Double>> debugPoints2() {
        List<List<Double>> mockFieldPoints = new ArrayList<>();

        List<Double> point = new ArrayList<>();
        point.add(20.18139865033877);point.add(39.61342882122592);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(20.18147432937476);point.add(39.613922170673284);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(20.18193142533871);point.add(39.61431098763446);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(20.18194527565025);point.add(39.61448256844796);
        mockFieldPoints.add(point);

        point = new ArrayList<>();
        point.add(20.18139865033877);point.add(39.61342882122592);
        mockFieldPoints.add(point);


        List<List<Double>> mockFieldPointsInvert = new ArrayList<>();
        for(int i = (mockFieldPoints.size()-1);i>=0;i--){
            mockFieldPointsInvert.add(mockFieldPoints.get(i));
        }
        return mockFieldPointsInvert;
    }
}
