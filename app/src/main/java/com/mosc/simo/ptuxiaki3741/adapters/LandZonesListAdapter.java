package com.mosc.simo.ptuxiaki3741.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.HashSet;
import java.util.List;

public class LandZonesListAdapter extends RecyclerView.Adapter<LandZonesListAdapter.LandZoneItem>{
    private final Land land;
    private final List<LandZone> data;
    private final ActionResult<LandZone> onClick;
    private final ActionResult<LandZone> onLongClick;
    private boolean showCheckMark;
    protected HashSet<MapView> mMapViews = new HashSet<>();

    public LandZonesListAdapter(
            Land land,
            List<LandZone> data,
            ActionResult<LandZone> onClick,
            ActionResult<LandZone> onLongClick
    ){
        this.land = land;
        this.data = data;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
        this.showCheckMark = false;
    }

    @NonNull @Override public LandZoneItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_land,parent,false);
        LandZoneItem holder = new LandZoneItem(view, parent.getContext());
        mMapViews.add(holder.binding.mapView);
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull LandZoneItem holder, int position) {
        LandZone zone = data.get(position);
        holder.itemView.setTag(zone);

        String display = zone.toString();
        holder.binding.tvLandName.setText(display);
        holder.binding.ctvLandName.setText(display);
        holder.binding.ctvLandName.setChecked(zone.isSelected());
        if(showCheckMark){
            holder.binding.tvLandName.setVisibility(View.GONE);
            holder.binding.ctvLandName.setVisibility(View.VISIBLE);
        }else{
            holder.binding.ctvLandName.setVisibility(View.GONE);
            holder.binding.tvLandName.setVisibility(View.VISIBLE);
        }
        if(onClick != null){
            holder.binding.item.setOnClickListener(v ->
                    onClick.onActionResult(zone)
            );
        }
        if(onLongClick != null){
            holder.binding.item.setOnLongClickListener(v -> {
                onLongClick.onActionResult(zone);
                return true;
            });
        }
        holder.setData(land,zone);
    }

    @Override public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void setShowCheckMark(boolean showCheckMark){
        this.showCheckMark = showCheckMark;
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    protected static class LandZoneItem extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public final  ViewHolderLandBinding binding;
        private final Context parentContext;

        private LandData land;
        private LandZoneData zone;

        public LandZoneItem(View view, Context parentContext)  {
            super(view);
            this.parentContext = parentContext;

            land = null;
            zone = null;

            binding = ViewHolderLandBinding.bind(itemView);
            binding.mapView.setTag(null);

            binding.mapView.setClickable(false);
            binding.mapView.onCreate(null);
            binding.mapView.getMapAsync(this);
        }
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            MapsInitializer.initialize(parentContext);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            binding.mapView.setTag(googleMap);
            initMap();
        }

        public void setData(Land land, LandZone zone){
            if(land == null) return;
            if(zone == null) return;
            if(land.getData() == null) return;
            if(zone.getData() == null) return;

            this.land = land.getData();
            this.zone = zone.getData();
            initMap();
        }

        private void initMap(){
            if(binding.mapView.getTag() == null) return;
            if(land == null || zone == null) return;

            GoogleMap googleMap = (GoogleMap) binding.mapView.getTag();

            googleMap.clear();
            if(googleMap.getMapType() != GoogleMap.MAP_TYPE_NONE){
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }

            if(land.getBorder().size() > 0){
                googleMap.addPolygon(LandUtil.getPolygonOptions(land,false).zIndex(1));
                if(zone.getBorder().size() > 0){
                    googleMap.addPolygon(LandUtil.getPolygonOptions(zone,false).zIndex(2));
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng point : land.getBorder()){
                    builder.include(point);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        AppValues.defaultPaddingLite
                ));
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        }
    }
}
