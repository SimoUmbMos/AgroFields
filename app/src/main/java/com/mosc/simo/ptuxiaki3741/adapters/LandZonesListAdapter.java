package com.mosc.simo.ptuxiaki3741.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.TextView;

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

    public void setShowCheckMark(boolean showCheckMark){
        this.showCheckMark = showCheckMark;
    }

    @NonNull @Override public LandZoneItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_land,parent,false);
        LandZoneItem holder = new LandZoneItem(parent.getContext(), view);
        mMapViews.add(holder.mapView);
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull LandZoneItem holder, int position) {
        LandZone zone = data.get(position);
        holder.itemView.setTag(zone);

        String display = zone.toString();
        holder.ctvLandName.setText(display);
        holder.tvLandName.setText(display);
        holder.ctvLandName.setChecked(zone.isSelected());
        if(showCheckMark){
            holder.tvLandName.setVisibility(View.INVISIBLE);
            holder.ctvLandName.setVisibility(View.VISIBLE);
        }else{
            holder.ctvLandName.setVisibility(View.INVISIBLE);
            holder.tvLandName.setVisibility(View.VISIBLE);
        }
        if(onClick != null){
            holder.item.setOnClickListener(v ->
                    onClick.onActionResult(zone)
            );
        }
        if(onLongClick != null){
            holder.item.setOnLongClickListener(v -> {
                onLongClick.onActionResult(zone);
                return true;
            });
        }
        holder.setData(land,zone);
    }

    @Override public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    protected static class LandZoneItem extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public TextView tvLandName;
        public CheckedTextView ctvLandName;
        public FrameLayout item;
        public MapView mapView;

        public GoogleMap mMap;

        protected LandData mLandData;
        protected LandZoneData mZoneData;

        private Context mContext;

        public LandZoneItem(Context context, View view)  {
            super(view);
            mContext = context;

            ViewHolderLandBinding binding = ViewHolderLandBinding.bind(itemView);
            item = binding.item;
            ctvLandName = binding.ctvLandName;
            tvLandName = binding.tvLandName;
            mapView = binding.mapView;

            mapView.setClickable(false);
            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            MapsInitializer.initialize(mContext);
            mMap.getUiSettings().setMapToolbarEnabled(false);

            if(mLandData != null && mZoneData != null){
                initMap();
            }
        }

        public void setData(Land land, LandZone zone){
            mLandData = null;
            mZoneData = null;

            if(land == null) return;
            if(land.getData() == null) return;
            mLandData = land.getData();

            if(zone == null) return;
            if(zone.getData() == null) return;
            mZoneData = zone.getData();

            if(mMap != null){
                initMap();
            }
        }

        private void initMap(){
            mMap.clear();
            mMap.addPolygon(LandUtil.getPolygonOptions(mLandData,false));
            mMap.addPolygon(LandUtil.getPolygonOptions(mZoneData,false));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng point : mLandData.getBorder()){
                builder.include(point);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPaddingLite
            ));
        }
    }
}
