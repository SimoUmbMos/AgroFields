package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.android.material.card.MaterialCardView;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.values.AppValues;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandWithTagsBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandItem>{
    private final List<Land> data;
    private final ActionResult<Land> onLandClick;
    private final ActionResult<Land> onLandLongClick;
    private boolean showCheckMark;
    protected HashSet<MapView> mMapViews = new HashSet<>();

    public LandListAdapter(
            ActionResult<Land> onLandClick,
            ActionResult<Land> onLandLongClick
    ){
        this.data = new ArrayList<>();
        this.onLandClick = onLandClick;
        this.onLandLongClick = onLandLongClick;
        this.showCheckMark = false;
    }

    @NonNull @Override public LandItem onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_land_with_tags, parent, false);
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
            holder.binding.llTagsContainer.setOnClickListener(v ->
                onLandClick.onActionResult(land)
            );
        }
        if(onLandLongClick != null){
            holder.binding.item.setOnLongClickListener(v -> {
                onLandLongClick.onActionResult(land);
                return true;
            });
            holder.binding.llTagsContainer.setOnLongClickListener(v -> {
                onLandLongClick.onActionResult(land);
                return true;
            });
        }

        holder.setLand(land);
    }

    @Override public int getItemCount() {
        return data.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    public void saveData(List<Land> data){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LandDiffUtil(this.data, data, showCheckMark, showCheckMark));
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }
    public void saveData(List<Land> data, boolean showCheckMark){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LandDiffUtil(this.data, data, this.showCheckMark, showCheckMark));
        this.showCheckMark = showCheckMark;
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    protected static class LandItem extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public final  ViewHolderLandWithTagsBinding binding;
        private final Context parentContext;

        public LandItem(View view, Context parentContext) {
            super(view);
            binding = ViewHolderLandWithTagsBinding.bind(itemView);
            this.parentContext = parentContext;

            binding.hsvTagsParent.setVisibility(View.GONE);

            binding.mapView.setTag(null);
            binding.getRoot().setTag(null);

            binding.mapView.setClickable(false);
            binding.mapView.onCreate(null);
            binding.mapView.getMapAsync(this);
        }

        public void setLand(Land land){
            if(land == null) return;
            if(land.getData() == null) return;

            int smallDistance = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2,
                    parentContext.getResources().getDisplayMetrics()
            );
            int normalDistance = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    4,
                    parentContext.getResources().getDisplayMetrics()
            );
            int largeDistance = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    parentContext.getResources().getDisplayMetrics()
            );
            List<String> tags = LandUtil.getLandTags(land.getData());
            binding.llTagsContainer.removeAllViews();
            for(int i = 0; i < tags.size(); i++){
                if( tags.get(i) == null ) continue;
                MaterialCardView cardView = new MaterialCardView(parentContext);
                cardView.setCardElevation(4);
                TextView tagView = new TextView(parentContext);
                tagView.setText(tags.get(i));

                LinearLayout.LayoutParams pText  = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                pText.setMargins(largeDistance,smallDistance,largeDistance,smallDistance);
                tagView.setLayoutParams(pText);

                LinearLayout.LayoutParams pCard  = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                if(i == 0){
                    pCard.setMargins(normalDistance,normalDistance,normalDistance,normalDistance);
                }else{
                    pCard.setMargins(0,normalDistance,normalDistance,normalDistance);
                }
                cardView.setLayoutParams(pCard);

                cardView.addView(tagView);
                binding.llTagsContainer.addView(cardView);
            }
            if(binding.llTagsContainer.getChildCount() > 0){
                binding.hsvTagsParent.setVisibility(View.VISIBLE);
            }else{
                binding.hsvTagsParent.setVisibility(View.GONE);
            }

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

    private static class LandDiffUtil extends DiffUtil.Callback {
        private final List<Land> oldList;
        private final List<Land> newList;
        private final boolean oldCheckbox;
        private final boolean newCheckbox;

        public LandDiffUtil(List<Land> oldList, List<Land> newList, boolean oldCheckbox, boolean newCheckbox){
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
            try {
                if(oldCheckbox != newCheckbox)
                    return false;

                Land land1 = oldList.get(oldItemPosition);
                Land land2 = newList.get(newItemPosition);
                if(land1 == null || land2 == null)
                    return false;
                if(land1.getData() == null || land2.getData() == null)
                    return false;
                if(land1.isSelected() != land2.isSelected())
                    return false;
                if(!land1.getData().getColor().toString().equals(land2.getData().getColor().toString()))
                    return false;
                if(!land1.getData().getTitle().equals(land2.getData().getTitle()))
                    return false;
                if(!ListUtils.arraysMatch(land1.getData().getBorder(),land2.getData().getBorder()))
                    return false;
                if(!ListUtils.arraysMatch(land1.getData().getHoles(),land2.getData().getHoles()))
                    return false;
            }catch (Exception e){
                return false;
            }
            return true;
        }
    }
}
