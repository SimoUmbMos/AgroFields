package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandBinding;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LandZonesListAdapter extends RecyclerView.Adapter<LandZonesListAdapter.LandZoneItem>{
    private final LandData land;
    private final List<LandZone> data;
    private final ActionResult<LandZone> onClick;
    private final ActionResult<LandZone> onLongClick;
    private boolean showCheckMark;
    protected HashSet<MapView> mMapViews = new HashSet<>();

    public LandZonesListAdapter(
            Land land,
            ActionResult<LandZone> onClick,
            ActionResult<LandZone> onLongClick
    ){
        this.data = new ArrayList<>();
        if(land != null){
            this.land = land.getData();
        }else{
            this.land = null;
        }
        this.onClick = onClick;
        this.onLongClick = onLongClick;
        this.showCheckMark = false;
    }

    @NonNull @Override public LandZoneItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_land,parent,false);
        LandZoneItem holder = new LandZoneItem(view, parent.getContext(), land);
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
        holder.setData(zone);
    }

    @Override public int getItemCount() {
        return data.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    public void saveData(List<LandZone> data){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new LandZonesDiffUtil(
                        this.data, data,
                        this.showCheckMark,this.showCheckMark
                )
        );
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    public void saveData(List<LandZone> data, boolean showCheckMark){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new LandZonesDiffUtil(
                        this.data, data,
                        this.showCheckMark, showCheckMark
                )
        );
        this.showCheckMark = showCheckMark;
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    protected static class LandZoneItem extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public final  ViewHolderLandBinding binding;
        private final Context parentContext;

        private final LandData land;
        private LandZoneData zone;

        public LandZoneItem(View view, Context parentContext, LandData land)  {
            super(view);
            this.parentContext = parentContext;

            this.land = land;
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

        public void setData(LandZone zone){
            if(zone == null) return;
            if(zone.getData() == null) return;

            this.zone = zone.getData();
            initMap();
        }

        private void initMap(){
            if(binding.mapView.getTag() == null) return;
            if(zone == null) return;

            GoogleMap googleMap = (GoogleMap) binding.mapView.getTag();

            googleMap.clear();
            if(googleMap.getMapType() != GoogleMap.MAP_TYPE_NONE){
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }

            if(land != null && land.getBorder().size() > 0){
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
            }else if(zone.getBorder().size() > 0){

                googleMap.addPolygon(LandUtil.getPolygonOptions(zone,false).zIndex(2));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng point : zone.getBorder()){
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

    private static class LandZonesDiffUtil extends DiffUtil.Callback {
        private final List<LandZone> oldList;
        private final List<LandZone> newList;
        private final boolean oldCheckbox;
        private final boolean newCheckbox;

        public LandZonesDiffUtil(
                List<LandZone> oldList,
                List<LandZone> newList,
                boolean oldCheckbox,
                boolean newCheckbox
        ) {
            this.oldList = oldList;
            this.newList = newList;
            this.oldCheckbox = oldCheckbox;
            this.newCheckbox = newCheckbox;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return DataUtil.checkItemsTheSame(oldList.get(oldItemPosition),newList.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            try{
                if(oldCheckbox != newCheckbox)
                    return false;

                LandZone zone1 = oldList.get(oldItemPosition);
                LandZone zone2 = newList.get(newItemPosition);
                if(zone1 == null || zone2 == null)
                    return false;
                if(zone1.getData() == null || zone2.getData() == null)
                    return false;
                if(zone1.isSelected() != zone2.isSelected())
                    return false;
                if(zone1.getData().getLid() != zone2.getData().getLid())
                    return false;
                if(!zone1.getData().getTitle().equals(zone2.getData().getTitle()))
                    return false;
                if(!zone1.getData().getNote().equals(zone2.getData().getNote()))
                    return false;
                if(!zone1.getData().getColor().toString().equals(zone2.getData().getColor().toString()))
                    return false;
                if(!ListUtils.arraysMatch(zone1.getData().getBorder(),zone2.getData().getBorder()))
                    return false;
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }
}