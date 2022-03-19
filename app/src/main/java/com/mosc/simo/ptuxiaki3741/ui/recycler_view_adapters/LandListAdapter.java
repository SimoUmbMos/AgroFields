package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.ListUtils;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
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

    @Override
    public void onViewRecycled(@NonNull LandItem holder) {
        mMapViews.remove(holder.binding.mapView);
        super.onViewRecycled(holder);
    }

    @Override public void onBindViewHolder(@NonNull LandItem holder, int position) {
        Land land = data.get(position);
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

        holder.setLand(land,showCheckMark);
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

    protected static class LandItem extends RecyclerView.ViewHolder{
        public final  ViewHolderLandWithTagsBinding binding;
        private LandData landData;
        private GoogleMap googleMap;

        public LandItem(View view, Context parentContext) {
            super(view);
            binding = ViewHolderLandWithTagsBinding.bind(itemView);

            landData = null;
            googleMap = null;

            binding.mapView.setClickable(false);
            binding.mapView.onCreate(null);

            MapsInitializer.initialize(
                    parentContext,
                    MapsInitializer.Renderer.LATEST,
                    r -> binding.mapView.getMapAsync(this::setMap)
            );
        }

        public void setMap(GoogleMap gMap) {
            googleMap = gMap;
            if(googleMap == null) return;
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.075368, 23.553767),16));
            draw();
            googleMap.setOnMapLoadedCallback(this::draw);
        }

        public void setLand(Land land, boolean showCheckMark){
            if(land == null) return;
            if(land.getData() == null) return;

            String titleDisplay = land.toString();
            List<String> tags = LandUtil.getLandTags(land.getData());
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < tags.size(); i++){
                if( tags.get(i) == null ) continue;
                builder.append("#");
                builder.append(tags.get(i));
                if(i != (tags.size()-1))
                    builder.append(" ");
            }
            String tagsDisplay = builder.toString();

            binding.tvLandName.setText(titleDisplay);
            binding.tvLandName.setSelected(true);
            binding.cbSelect.setChecked(land.isSelected());
            if(showCheckMark){
                binding.cbSelect.setVisibility(View.VISIBLE);
                if(land.isSelected()){
                    binding.getRoot().setStrokeWidth(UIUtil.dpToPx(binding.getRoot().getContext(), 2));
                }else{
                    binding.getRoot().setStrokeWidth(0);
                }
            }else{
                binding.cbSelect.setVisibility(View.GONE);
                binding.getRoot().setStrokeWidth(0);
            }
            ColorData color = land.getData().getColor();
            if(color != null){
                binding.getRoot().setCardBackgroundColor(color.getColor());
                if(UIUtil.showBlackText(color)){
                    binding.tvLandName.setTextColor(Color.BLACK);
                    binding.tvTagsContainer.setTextColor(Color.BLACK);
                    binding.cbSelect.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
                }else{
                    binding.tvLandName.setTextColor(Color.WHITE);
                    binding.tvTagsContainer.setTextColor(Color.WHITE);
                    binding.cbSelect.setButtonTintList(ColorStateList.valueOf(Color.WHITE));
                }
            }
            binding.tvTagsContainer.setText(tagsDisplay);
            binding.tvTagsContainer.setSelected(true);
            if(tagsDisplay.isEmpty()){
                binding.tvTagsContainer.setVisibility(View.GONE);
            }else{
                binding.tvTagsContainer.setVisibility(View.VISIBLE);
            }

            landData = land.getData();
            draw();
        }

        public void draw(){
            if(googleMap == null) return;
            googleMap.clear();
            if(landData != null && landData.getBorder().size() > 0){
                LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                for(LatLng point : landData.getBorder()){
                    bounds.include(point);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),AppValues.defaultPaddingLite));
                googleMap.addPolygon(LandUtil.getPolygonOptions(landData,false));
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
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
