package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.List;

public class LandZonesListAdapter extends RecyclerView.Adapter<LandZonesListAdapter.LandZoneItem>{
    private final List<LandZone> data;
    private final ActionResult<LandZone> onClick;
    private final ActionResult<LandZone> onLongClick;
    private boolean showCheckMark;

    public LandZonesListAdapter(
            List<LandZone> data,
            ActionResult<LandZone> onClick,
            ActionResult<LandZone> onLongClick
    ){
        this.data = data;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
        this.showCheckMark = false;
    }

    public void setShowCheckMark(
            boolean showCheckMark
    ){
        this.showCheckMark = showCheckMark;
    }

    @NonNull @Override public LandZoneItem onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_land,parent,false);
        return new LandZoneItem(view);
    }

    @Override public void onBindViewHolder(
            @NonNull LandZoneItem holder,
            int position
    ) {
        if(data.size()>position){
            LandZone landZone = data.get(position);
            if(landZone.getData() != null){
                holder.bindData(showCheckMark,landZone,onClick,onLongClick);
            }
        }
    }
    @Override
    public void onViewRecycled(LandZoneItem holder) {
        if (holder.mMap != null) {
            holder.mMap.clear();
        }
    }

    @Override public int getItemCount() {
        if(data != null)
            return data.size();
        else
            return 0;
    }

    protected static class LandZoneItem extends RecyclerView.ViewHolder {
        public final ViewHolderLandBinding binding;
        private final int strokeColor;
        private final int fillColor;

        public GoogleMap mMap;

        public LandZoneItem(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderLandBinding.bind(itemView);
            binding.mapView.onCreate(null);
            binding.mapView.onResume();
            if(itemView.getContext() != null){
                strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.polygonStroke);
                fillColor = ContextCompat.getColor(itemView.getContext(), R.color.polygonFill);
            }else{
                strokeColor = AppValues.strokeColor;
                fillColor = AppValues.fillColor;
            }
        }
        public void bindData(
            boolean showCheckBox,
            LandZone landZone,
            ActionResult<LandZone> onClick,
            ActionResult<LandZone> onLongClick
        ){
            String display = landZone.getData().getTitle() +
                    " #" +
                    EncryptUtil.convert4digit(landZone.getData().getId());
            if(showCheckBox){
                binding.ctvLandName.setVisibility(View.VISIBLE);
                binding.tvLandName.setVisibility(View.INVISIBLE);
            }else{
                binding.ctvLandName.setVisibility(View.INVISIBLE);
                binding.tvLandName.setVisibility(View.VISIBLE);
            }
            binding.ctvLandName.setText(display);
            binding.tvLandName.setText(display);
            binding.ctvLandName.setChecked(landZone.isSelected());

            binding.item.setOnClickListener(v ->
                    onClick.onActionResult(landZone)
            );
            binding.item.setOnLongClickListener(v -> {
                onLongClick.onActionResult(landZone);
                return true;
            });
            if(mMap != null){
                zoomOnLand(landZone);
                drawOnMap(landZone);
            }else{
                binding.mapView.getMapAsync(googleMap -> {
                    MapsInitializer.initialize(binding.getRoot().getContext());
                    mMap = googleMap;
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    zoomOnLand(landZone);
                    drawOnMap(landZone);
                });
            }
        }
        private void drawOnMap(LandZone land){
            mMap.clear();
            if(land.getData().getBorder().size()>0){
                PolygonOptions options = new PolygonOptions();
                options.addAll(land.getData().getBorder());
                options.fillColor(fillColor);
                options.strokeColor(strokeColor);
                options.clickable(false);
                mMap.addPolygon(options);
            }
        }
        private void zoomOnLand(LandZone land){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng point : land.getData().getBorder()){
                builder.include(point);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPadding
            ));
        }
    }
}
