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
    public void setShowCheckMark(boolean showCheckMark){
        this.showCheckMark = showCheckMark;
    }

    @NonNull @Override public LandItem onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_land, parent, false);
        LandItem holder = new LandItem(parent.getContext(),view);
        mMapViews.add(holder.mapView);
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull LandItem holder, int position) {
        Land land = data.get(position);
        holder.itemView.setTag(land);

        String display = land.toString();
        holder.tvLandName.setText(display);
        holder.ctvLandName.setText(display);
        holder.ctvLandName.setChecked(land.isSelected());
        if(showCheckMark){
            holder.tvLandName.setVisibility(View.INVISIBLE);
            holder.ctvLandName.setVisibility(View.VISIBLE);
        }else{
            holder.ctvLandName.setVisibility(View.INVISIBLE);
            holder.tvLandName.setVisibility(View.VISIBLE);
        }
        if(onLandClick != null){
            holder.item.setOnClickListener(v ->
                    onLandClick.onActionResult(land)
            );
        }
        if(onLandLongClick != null){
            holder.item.setOnLongClickListener(v -> {
                onLandLongClick.onActionResult(land);
                return true;
            });
        }

        holder.setLand(land);
    }

    @Override public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    protected static class LandItem extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public TextView tvLandName;
        public CheckedTextView ctvLandName;
        public FrameLayout item;
        public MapView mapView;

        protected GoogleMap mMap;
        protected LandData mData;

        private Context mContext;

        public LandItem(Context context, View view) {
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

            if(mData != null){
                initMap();
            }
        }

        public void setLand(Land land){
            mData = null;

            if(land == null) return;
            if(land.getData() == null) return;
            mData = land.getData();

            if(mMap != null){
                initMap();
            }
        }

        private void initMap(){
            mMap.clear();
            mMap.addPolygon(LandUtil.getPolygonOptions(mData,false));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng point : mData.getBorder()){
                builder.include(point);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    AppValues.defaultPaddingLite
            ));
        }
    }
}
