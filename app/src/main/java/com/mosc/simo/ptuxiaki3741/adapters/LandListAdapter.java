package com.mosc.simo.ptuxiaki3741.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandItem>{
    private final List<Land> data;
    private final ActionResult<Land> onLandClick;
    private final ActionResult<Land> onLandLongClick;
    private boolean showCheckMark;

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
    public void setShowCheckMark(
            boolean showCheckMark
    ){
        this.showCheckMark = showCheckMark;
    }

    @NonNull @Override public LandItem onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_land,parent,false);
        return new LandItem(view);
    }

    @Override public void onBindViewHolder(
            @NonNull LandItem holder,
            int position
    ) {
        if(data.size()>position){
            Land land = data.get(position);
            LandData landData = land.getData();
            if(landData != null){
                holder.bindData(showCheckMark,land,onLandClick,onLandLongClick);
            }
        }
    }
    @Override
    public void onViewRecycled(LandItem holder) {
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

    protected static class LandItem extends RecyclerView.ViewHolder {
        public final ViewHolderLandBinding binding;

        public GoogleMap mMap;

        public LandItem(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderLandBinding.bind(itemView);
            binding.mapView.onCreate(null);
            binding.mapView.onResume();
        }
        public void bindData(
            boolean showCheckBox,
            Land land,
            ActionResult<Land> onClick,
            ActionResult<Land> onLongClick
        ){
            String display = land.getData().getTitle() +
                    " #" +
                    EncryptUtil.convert4digit(land.getData().getId());
            if(showCheckBox){
                binding.ctvLandName.setVisibility(View.VISIBLE);
                binding.tvLandName.setVisibility(View.INVISIBLE);
            }else{
                binding.ctvLandName.setVisibility(View.INVISIBLE);
                binding.tvLandName.setVisibility(View.VISIBLE);
            }
            binding.ctvLandName.setText(display);
            binding.tvLandName.setText(display);
            binding.ctvLandName.setChecked(land.isSelected());

            binding.item.setOnClickListener(v ->
                    onClick.onActionResult(land)
            );
            if(onLongClick != null){
                binding.item.setOnLongClickListener(v -> {
                    onLongClick.onActionResult(land);
                    return true;
                });
            }
            if(mMap != null){
                zoomOnLand(land);
                drawOnMap(land);
            }else{
                binding.mapView.getMapAsync(googleMap -> {
                    MapsInitializer.initialize(binding.getRoot().getContext());
                    mMap = googleMap;
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    zoomOnLand(land);
                    drawOnMap(land);
                });
            }
        }
        private void drawOnMap(Land land){
            mMap.clear();
            if(land.getData().getBorder().size()>0){
                int strokeColor = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        AppValues.defaultLandColor.getRed(),
                        AppValues.defaultLandColor.getGreen(),
                        AppValues.defaultLandColor.getBlue()
                );
                int fillColor = Color.argb(
                        AppValues.defaultFillAlpha,
                        AppValues.defaultLandColor.getRed(),
                        AppValues.defaultLandColor.getGreen(),
                        AppValues.defaultLandColor.getBlue()
                );
                PolygonOptions options = new PolygonOptions();
                options.addAll(land.getData().getBorder());
                options.fillColor(fillColor);
                options.strokeColor(strokeColor);
                options.clickable(false);
                for(List<LatLng> hole:land.getData().getHoles()) {
                    options.addHole(hole);
                }
                mMap.addPolygon(options);
            }
        }
        private void zoomOnLand(Land land){
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
