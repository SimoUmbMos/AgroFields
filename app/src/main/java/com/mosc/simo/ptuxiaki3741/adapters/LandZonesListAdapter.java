package com.mosc.simo.ptuxiaki3741.adapters;

import android.graphics.Color;
import android.util.Log;
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
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import java.util.List;

public class LandZonesListAdapter extends RecyclerView.Adapter<LandZonesListAdapter.LandZoneItem>{
    private final Land land;
    private final List<LandZone> data;
    private final ActionResult<LandZone> onClick;
    private final ActionResult<LandZone> onLongClick;
    private boolean showCheckMark;

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
                holder.bindData(showCheckMark, land, landZone, onClick, onLongClick);
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
        private static final String TAG = "LandZoneItem";
        public final ViewHolderLandBinding binding;

        public GoogleMap mMap;

        public LandZoneItem(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderLandBinding.bind(itemView);
            binding.mapView.onCreate(null);
            binding.mapView.onResume();
        }
        public void bindData(
            boolean showCheckBox,
            Land land,
            LandZone landZone,
            ActionResult<LandZone> onClick,
            ActionResult<LandZone> onLongClick
        ){
            String display = landZone.getData().getTitle() +
                    " #" +
                    DataUtil.convert4digit(landZone.getData().getId());
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
                zoomOnLand(land);
                drawOnMap(land, landZone);
            }else{
                binding.mapView.getMapAsync(googleMap -> {
                    MapsInitializer.initialize(binding.getRoot().getContext());
                    mMap = googleMap;
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    zoomOnLand(land);
                    drawOnMap(land, landZone);
                });
            }
        }
        private void drawOnMap(Land land, LandZone zone){
            mMap.clear();
            if(land.getData().getBorder().size()>0){
                Log.d(TAG, "drawOnMap: draw land");
                int strokeColor1 = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        land.getData().getColor().getRed(),
                        land.getData().getColor().getGreen(),
                        land.getData().getColor().getBlue()
                );
                int fillColor1 = Color.argb(
                        AppValues.defaultFillAlpha,
                        land.getData().getColor().getRed(),
                        land.getData().getColor().getGreen(),
                        land.getData().getColor().getBlue()
                );
                PolygonOptions options1 = new PolygonOptions();
                options1.addAll(land.getData().getBorder());
                options1.strokeColor(strokeColor1);
                options1.fillColor(fillColor1);
                options1.clickable(false);
                options1.zIndex(1);
                mMap.addPolygon(options1);
            }
            if(zone.getData().getBorder().size()>0){
                Log.d(TAG, "drawOnMap: draw zone");
                int strokeColor2 = Color.argb(
                        AppValues.defaultStrokeAlpha,
                        zone.getData().getColor().getRed(),
                        zone.getData().getColor().getGreen(),
                        zone.getData().getColor().getBlue()
                );
                int fillColor2 = Color.argb(
                        AppValues.defaultFillAlpha,
                        zone.getData().getColor().getRed(),
                        zone.getData().getColor().getGreen(),
                        zone.getData().getColor().getBlue()
                );
                PolygonOptions options2 = new PolygonOptions();
                options2.addAll(zone.getData().getBorder());
                options2.strokeColor(strokeColor2);
                options2.fillColor(fillColor2);
                options2.clickable(false);
                options2.zIndex(2);
                mMap.addPolygon(options2);
            }
        }
        private void zoomOnLand(Land land){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int size = 0;
            for(LatLng point : land.getData().getBorder()){
                builder.include(point);
                size++;
            }
            if(size>0)
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        AppValues.defaultPadding
                ));
        }
    }
}
