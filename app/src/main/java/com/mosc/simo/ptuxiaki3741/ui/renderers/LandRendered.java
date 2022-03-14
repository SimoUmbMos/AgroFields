package com.mosc.simo.ptuxiaki3741.ui.renderers;

import android.annotation.SuppressLint;
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

public class LandRendered extends DefaultClusterRenderer<ClusterLand> {
    private final Context context;
    private final GoogleMap map;
    private final Map<ClusterLand, List<Polygon>> polygons;

    public LandRendered(Context context, GoogleMap map, ClusterManager<ClusterLand> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;
        polygons = new HashMap<>();
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterLand item, @NonNull MarkerOptions markerOptions) {
        setupMarker(item,markerOptions);
        removePolygons(item);
        addPolygons(item);
    }

    @Override
    protected void onBeforeClusterRendered(@NonNull Cluster<ClusterLand> cluster, @NonNull MarkerOptions markerOptions) {
        setupCluster(cluster,markerOptions);
        cluster.getItems().forEach(this::removePolygons);
    }

    @Override
    protected void onClusterItemUpdated(@NonNull ClusterLand item, @NonNull Marker marker) { }

    @Override
    protected void onClusterUpdated(@NonNull Cluster<ClusterLand> cluster, @NonNull Marker marker) { }

    private void addPolygons(ClusterLand item) {
        List<Polygon> itemPolygons = new ArrayList<>();

        Polygon polygon = map.addPolygon(
                LandUtil.getPolygonOptions(
                        item.getLandData(),
                        false
                ).zIndex(AppValues.liveMapLandZIndex)
        );

        itemPolygons.add(polygon);
        for(LandZoneData zone : item.getZonesData()){
            polygon = map.addPolygon(
                    LandUtil.getPolygonOptions(
                            zone,
                            false
                    ).zIndex(AppValues.liveMapZoneZIndex)
            );
            itemPolygons.add(polygon);
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

    private void setupMarker(@NonNull ClusterLand item, @NonNull MarkerOptions markerOptions) {
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

    @SuppressLint("SetTextI18n")
    private void setupCluster(@NonNull Cluster<ClusterLand> cluster, @NonNull MarkerOptions markerOptions) {
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setBackground(null);
        View inflatedView = View.inflate(context, R.layout.view_land_cluster, null);
        ViewLandClusterBinding binding = ViewLandClusterBinding.bind(inflatedView);
        if(cluster.getSize() > 99){
            binding.tvClusterTitle.setText("99+");
        }else{
            binding.tvClusterTitle.setText(String.valueOf(cluster.getSize()));
        }
        iconGenerator.setContentView(inflatedView);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        markerOptions.alpha(0.8f);
        markerOptions.zIndex(AppValues.liveMapClusterZIndex);
    }

}
