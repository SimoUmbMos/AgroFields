package com.mosc.simo.ptuxiaki3741.backend.file.wkt;

import com.google.android.gms.maps.model.LatLng;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;

import java.util.ArrayList;
import java.util.List;

public class WellKnownTextExporter {
    public static String WellKnownTextExport(List<Land> lands){
        StringBuilder builder = new StringBuilder();
        if(lands.size()>0){
            if(lands.size() > 1){
                builder.append("MULTIPOLYGON (");
                for(int i = 0; i < lands.size(); i++){
                    createMultiLandWKT(lands.get(i).getData(), builder);
                    if(i != lands.size()-1){
                        builder.append(", ");
                    }
                }
                builder.append(")");
            }else{
                createLandWKT(lands.get(0).getData(), builder);
            }
        }else{
            builder.append("POLYGON EMPTY");
        }
        return builder.toString();
    }

    private static void createLandWKT(LandData data, StringBuilder builder) {
        builder.append("POLYGON ");
        List<List<LatLng>> tempData = new ArrayList<>(data.getHoles());
        tempData.add(0,data.getBorder());
        boolean isEmpty = tempData.isEmpty();
        if(isEmpty){
            builder.append("EMPTY");
        }else{
            builder.append("(");
            for(int i = 0; i < tempData.size(); i++){
                builder.append("(");
                for(int j = 0; j < tempData.get(i).size(); j++){
                    builder.append(tempData.get(i).get(j).latitude);
                    builder.append(" ");
                    builder.append(tempData.get(i).get(j).longitude);
                    builder.append(", ");
                }
                builder.append(tempData.get(i).get(0).latitude);
                builder.append(" ");
                builder.append(tempData.get(i).get(0).longitude);
                builder.append(")");
                if(i != tempData.size()-1){
                    builder.append(", ");
                }
            }
            builder.append(")");
        }
    }

    private static void createMultiLandWKT(LandData data, StringBuilder builder) {
        List<List<LatLng>> tempData = new ArrayList<>(data.getHoles());
        tempData.add(0,data.getBorder());
        boolean isEmpty = tempData.isEmpty();
        if(!isEmpty) {
            builder.append("(");
            for(int i = 0; i < tempData.size(); i++){
                builder.append("(");
                for(int j = 0; j < tempData.get(i).size(); j++){
                    builder.append(tempData.get(i).get(j).latitude);
                    builder.append(" ");
                    builder.append(tempData.get(i).get(j).longitude);
                    builder.append(", ");
                }
                builder.append(tempData.get(i).get(0).latitude);
                builder.append(" ");
                builder.append(tempData.get(i).get(0).longitude);
                builder.append(")");
                if(i != tempData.size()-1){
                    builder.append(", ");
                }
            }
            builder.append(")");
        }
    }
}
