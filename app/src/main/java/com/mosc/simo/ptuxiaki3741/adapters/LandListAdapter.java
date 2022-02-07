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
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.HashSet;
import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandItem>{
    private final List<Land> data;
    private final ActionResult<Land> onLandClick;
    private final ActionResult<Land> onLandLongClick;
    private boolean showCheckMark;
    protected HashSet<MapView> mMapViews = new HashSet<>();

    public LandListAdapter(
            List<Land> data,
            ActionResult<Land> onLandClick,
            ActionResult<Land> onLandLongClick
    ){
        this.data = data;
        this.onLandClick = onLandClick;
        this.onLandLongClick = onLandLongClick;
        this.showCheckMark = false;
    }

    public LandListAdapter(
            List<Land> data,
            ActionResult<Land> onLandClick
    ){
        this.data = data;
        this.onLandClick = onLandClick;
        this.onLandLongClick = null;
        this.showCheckMark = false;
    }

    @NonNull @Override public LandItem onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_land, parent, false);
        LandItem holder = new LandItem(view, parent.getContext());
        mMapViews.add(holder.binding.mapView);
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull LandItem holder, int position) {
        Land land = data.get(position);
        String display = land.toString();
        holder.binding.tvLandName.setText(display);
        holder.binding.ctvLandName.setText(display);
        holder.binding.ctvLandName.setChecked(land.isSelected());
        if(showCheckMark){
            holder.binding.tvLandName.setVisibility(View.GONE);
            holder.binding.ctvLandName.setVisibility(View.VISIBLE);
        }else{
            holder.binding.ctvLandName.setVisibility(View.GONE);
            holder.binding.tvLandName.setVisibility(View.VISIBLE);
        }
        if(onLandClick != null){
            holder.binding.item.setOnClickListener(v ->
                    onLandClick.onActionResult(land)
            );
        }
        if(onLandLongClick != null){
            holder.binding.item.setOnLongClickListener(v -> {
                onLandLongClick.onActionResult(land);
                return true;
            });
        }

        holder.setLand(land);
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

    protected static class LandItem extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public final  ViewHolderLandBinding binding;
        private final Context parentContext;

        public LandItem(View view, Context parentContext) {
            super(view);
            binding = ViewHolderLandBinding.bind(itemView);
            this.parentContext = parentContext;

            binding.mapView.setTag(null);
            binding.getRoot().setTag(null);

            binding.mapView.setClickable(false);
            binding.mapView.onCreate(null);
            binding.mapView.getMapAsync(this);
        }

        public void setLand(Land land){
            if(land == null) return;
            if(land.getData() == null) return;
            binding.getRoot().setTag(land.getData());
            initMap();
        }

        private void initMap(){
            if(binding.mapView.getTag() == null) return;
            if(binding.getRoot().getTag() == null) return;

            GoogleMap googleMap = (GoogleMap) binding.mapView.getTag();

            googleMap.clear();
            if(googleMap.getMapType() != GoogleMap.MAP_TYPE_NONE){
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }

            LandData data = (LandData) binding.getRoot().getTag();
            if(data.getBorder().size() > 0){
                googleMap.addPolygon(LandUtil.getPolygonOptions(data,false));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng point : data.getBorder()){
                    builder.include(point);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        AppValues.defaultPaddingLite
                ));
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            MapsInitializer.initialize(parentContext);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            binding.mapView.setTag(googleMap);
            initMap();
        }
    }
}
