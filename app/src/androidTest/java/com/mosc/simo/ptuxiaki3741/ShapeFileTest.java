package com.mosc.simo.ptuxiaki3741;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.backend.file.extensions.shapefile.MyShapeFileReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class ShapeFileTest {
    public static final String TAG = "ShapeFileTest";
    private InputStream in;
    @Before
    public void init(){
        in = null;
        if(this.getClass().getClassLoader() != null ){
            in = this.getClass().getClassLoader().getResourceAsStream("map.shp");
        }
    }

    @Test
    public void testInputStreamExist(){
        assertNotEquals(null,in);
        if(in != null){
            List<List<LatLng>> lands  = MyShapeFileReader.readShapeFileReader(in);
            Log.d(TAG, "lands size: "+lands.size());
            Log.d(TAG, " ");
            for(List<LatLng> land : lands){
                Log.d(TAG, "land size: "+land.size());
                Log.d(TAG, " ");
            }
        }
    }

}
