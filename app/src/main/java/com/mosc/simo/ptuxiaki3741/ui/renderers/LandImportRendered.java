package com.mosc.simo.ptuxiaki3741.ui.renderers;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.models.ClusterLand;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.ViewLandClusterBinding;
import com.mosc.simo.ptuxiaki3741.databinding.ViewLandMarkerBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandImportRendered extends DefaultClusterRenderer<ClusterLand> {
    private final Context context;
    private final GoogleMap map;
    private final Map<ClusterLand, List<Polygon>> polygons = new HashMap<>();

    public LandImportRendered(Context context, GoogleMap map, ClusterManager<ClusterLand> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterLand item, @NonNull MarkerOptions markerOptions) {
        removePolygons(item);
        addPolygons(item);
        setupMarkerOptions(item,markerOptions);
    }

    @Override
    protected void onBeforeClusterRendered(@NonNull Cluster<ClusterLand> cluster, @NonNull MarkerOptions markerOptions) {
        cluster.getItems().forEach(this::removePolygons);
        setupMarkerOptions(cluster,markerOptions);
    }

    @Override
    protected void onClusterItemUpdated(@NonNull ClusterLand item, @NonNull Marker marker) {
        setupMarkerOptions(item,marker);
    }

    @Override
    protected void onClusterUpdated(@NonNull Cluster<ClusterLand> cluster, @NonNull Marker marker) {
        setupMarkerOptions(cluster,marker);
    }

    private void addPolygons(ClusterLand item) {
        if(item == null) return;

        List<Polygon> itemPolygons = new ArrayList<>();
        if(item.getLandData() != null){
            Polygon polygon = map.addPolygon(
                    LandUtil.getPolygonOptions(
                            item.getLandData(),
                            true
                    ).zIndex(AppValues.liveMapLandZIndex)
            );
            polygon.setTag(item);
            itemPolygons.add(polygon);
            for(LandZoneData zone : item.getZonesData()){
                if(zone == null) continue;
                polygon = map.addPolygon(
                        LandUtil.getPolygonOptions(
                                zone,
                                true
                        ).zIndex(AppValues.liveMapZoneZIndex)
                );
                polygon.setTag(item);
                itemPolygons.add(polygon);
            }
        }

        this.polygons.put(item,itemPolygons);
    }

    private void removePolygons(ClusterLand item) {
        List<Polygon> itemPolygons = this.polygons.getOrDefault(item,null);
        if(itemPolygons == null) return;
        for(Polygon polygon : itemPolygons){
            polygon.remove();
        }
        this.polygons.remove(item);
    }

    private void setupMarkerOptions(@NonNull ClusterLand item, @NonNull MarkerOptions markerOptions) {
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setBackground(null);
        View inflatedView = View.inflate(context, R.layout.view_land_marker, null);
        ViewLandMarkerBinding binding = ViewLandMarkerBinding.bind(inflatedView);
        binding.tvMarkerTitle.setText(item.getTitle());
        iconGenerator.setContentView(inflatedView);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        markerOptions.alpha(0.8f);
        markerOptions.zIndex(AppValues.liveMapMarkerZIndex);
    }

    private void setupMarkerOptions(@NonNull ClusterLand item, @NonNull Marker marker) {
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setBackground(null);
        View inflatedView = View.inflate(context, R.layout.view_land_marker, null);
        ViewLandMarkerBinding binding = ViewLandMarkerBinding.bind(inflatedView);
        binding.tvMarkerTitle.setText(item.getTitle());
        iconGenerator.setContentView(inflatedView);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        marker.setAlpha(0.8f);
        marker.setZIndex(AppValues.liveMapMarkerZIndex);
    }

    private void setupMarkerOptions(@NonNull Cluster<ClusterLand> cluster, @NonNull Marker marker) {
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setBackground(null);
        View inflatedView = View.inflate(context, R.layout.view_land_cluster, null);
        ViewLandClusterBinding binding = ViewLandClusterBinding.bind(inflatedView);
        String display;
        if(cluster.getSize() > 99){
            display = "99+";
        }else{
            display = String.valueOf(cluster.getSize());
        }
        binding.tvClusterTitle.setText(display);
        iconGenerator.setContentView(inflatedView);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        marker.setAlpha(0.8f);
        marker.setZIndex(AppValues.liveMapClusterZIndex);
    }

    private void setupMarkerOptions(@NonNull Cluster<ClusterLand> cluster, @NonNull MarkerOptions markerOptions) {
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setBackground(null);
        View inflatedView = View.inflate(context, R.layout.view_land_cluster, null);
        ViewLandClusterBinding binding = ViewLandClusterBinding.bind(inflatedView);
        String display;
        if(cluster.getSize() > 99){
            display = "99+";
        }else{
            display = String.valueOf(cluster.getSize());
        }
        binding.tvClusterTitle.setText(display);
        iconGenerator.setContentView(inflatedView);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        markerOptions.alpha(0.8f);
        markerOptions.zIndex(AppValues.liveMapClusterZIndex);
    }

}
