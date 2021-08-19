package com.mosc.simo.ptuxiaki3741;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.file.helper.ExportFieldModel;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonExporter;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.geojson.GeoJsonReader;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GeoJsonUnitTest {

    @Test
    public void testGeoJsonPolygonExport() {
        List<ExportFieldModel> exportFields = new ArrayList<>();
        List<List<List<Double>>> listsOfPoints = new ArrayList<>();
        listsOfPoints.add(debugPoints1());
        exportFields.add(new ExportFieldModel("Π. ΗΠΕΙΡΟΥ",listsOfPoints));
        JSONObject export = GeoJsonExporter.geoJsonExport(exportFields);
        System.out.println(export.toString());
        assertNotEquals(null,export);
    }

    @Test
    public void testGeoJsonMultiPolygonExport() {
        List<ExportFieldModel> exportFields = new ArrayList<>();
        List<List<List<Double>>> listsOfPoints = new ArrayList<>();
        listsOfPoints.add(debugPoints1());
        listsOfPoints.add(debugPoints2());
        exportFields.add(new ExportFieldModel("Π. ΗΠΕΙΡΟΥ",listsOfPoints));
        JSONObject export = GeoJsonExporter.geoJsonExport(exportFields);
        System.out.println(export.toString());
        assertNotEquals(null,export);
    }

    @Test
    public void testGeoJsonImport(){
        List<List<LatLng>> pointsLists = GeoJsonReader.exec(mockGeoJson());
        assertEquals(15, pointsLists.get(0).size());
    }

    @SuppressWarnings("SameReturnValue")
    private String mockGeoJson(){
        return "{\"totalFeatures\":1,\"features\":[{\"geometry\":{\"coordinates\":[[[20.18139865033877,39.61342882122592],[20.18146819746075,39.61331402478754],[20.18328367000238,39.61413031766426],[20.183440965480575,39.614234564720526],[20.183543306853867,39.61440004987836],[20.18362473440505,39.614704449003064],[20.183476070988924,39.61487518688857],[20.183312896535178,39.61493286491791],[20.18296248886477,39.61497537606664],[20.182588080365502,39.61496305693437],[20.18228311820098,39.6148314330757],[20.18194527565025,39.61448256844796],[20.18193142533871,39.61431098763446],[20.18147432937476,39.613922170673284],[20.18139865033877,39.61342882122592]]],\"type\":\"Polygon\"},\"id\":\"7PCGFN-dVOntl-tNZ36g-Uabete\",\"geometry_name\":\"the_geom\",\"type\":\"Feature\",\"properties\":{\"PER\":\"test2\"}}],\"type\":\"FeatureCollection\"}";
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
