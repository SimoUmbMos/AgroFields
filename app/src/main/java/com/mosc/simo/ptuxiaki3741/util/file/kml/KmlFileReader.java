package com.mosc.simo.ptuxiaki3741.util.file.kml;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.util.TaskRunner;
import com.mosc.simo.ptuxiaki3741.util.file.kml.async.KmlParser;

import java.io.InputStream;
import java.util.List;


public class KmlFileReader {
    public interface KmlInterface{
        void onKmlResult(List<List<LatLng>> result);
    }
    public static void exec(InputStream is,KmlInterface kmlInterface) {
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(new KmlParser(is), kmlInterface::onKmlResult);
    }
}
