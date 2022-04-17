package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.data.enums.AreaMetrics;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.data.util.DataUtil;
import com.mosc.simo.ptuxiaki3741.data.util.LandUtil;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandAreaBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LandDimenAdapter extends RecyclerView.Adapter<LandDimenAdapter.LandItem>{
    private final List<Land> data = new ArrayList<>();
    private AreaMetrics metric = AreaMetrics.SquareMeter;
    private ActionResult<Land> onClick = null;

    @NonNull
    @Override
    public LandItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return LandItem.getNewInstance(LayoutInflater.from(parent.getContext()),parent);
    }

    @Override
    public void onBindViewHolder(@NonNull LandItem holder, int position) {
        Land land;
        if(position < data.size())
            land = data.get(position);
        else
            land = null;
        if(land == null || land.getData() == null)
            return;

        holder.bindData(land,metric);
        holder.binding.getRoot().setOnClickListener(v->{
            if(onClick != null) onClick.onActionResult(land);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Land> newData){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LandsAreaDiffUtil(this.data, newData));
        data.clear();
        data.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setMetric(AreaMetrics metric){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LandsAreaDiffUtil(this.data, this.metric, this.data, metric));
        this.metric = metric;
        diffResult.dispatchUpdatesTo(this);
    }

    public void setOnClick(ActionResult<Land> onClick){
        this.onClick = onClick;
    }

    public static class LandItem extends RecyclerView.ViewHolder{
        public ViewHolderLandAreaBinding binding;
        public LandItem(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderLandAreaBinding.bind(itemView);
        }
        public static LandItem getNewInstance(@NonNull LayoutInflater inflater, ViewGroup parent){
            ViewHolderLandAreaBinding binding = ViewHolderLandAreaBinding.inflate(inflater,parent,false);
            return new LandItem(binding.getRoot());
        }
        public void bindData(Land data, AreaMetrics metric) {
            binding.cbSelected.setChecked(data.isSelected());
            binding.tvLandTitle.setText(data.getData().toString());
            binding.tvLandTitle.setSelected(true);
            String metricSymbol = DataUtil.getAreaMetricSymbol(binding.getRoot().getContext(),metric);
            String displayArea = new DecimalFormat("#.##").format(LandUtil.landArea(data.getData()) * metric.dimensionToSquareMeter);
            if(!metricSymbol.isEmpty()) displayArea += " " + metricSymbol;
            binding.tvLandArea.setText(displayArea);
            binding.tvLandArea.setSelected(true);
        }
    }

    private static class LandsAreaDiffUtil extends DiffUtil.Callback {
        private final List<Land> oldData;
        private final List<Land> newData;
        private final AreaMetrics oldMetric;
        private final AreaMetrics newMetric;

        public LandsAreaDiffUtil(List<Land> oldData, List<Land> newData){
            this.oldData = oldData;
            this.newData = newData;
            oldMetric = AreaMetrics.SquareMeter;
            newMetric = AreaMetrics.SquareMeter;
        }

        public LandsAreaDiffUtil(List<Land> oldData, AreaMetrics oldMetric, List<Land> newData, AreaMetrics newMetric){
            this.oldData = oldData;
            this.oldMetric = oldMetric;
            this.newData = newData;
            this.newMetric = newMetric;
        }

        @Override
        public int getOldListSize() {
            return oldData.size();
        }

        @Override
        public int getNewListSize() {
            return newData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            boolean ans;
            try{
                long idOld = oldData.get(oldItemPosition).getData().getId();
                long idNew = newData.get(newItemPosition).getData().getId();
                ans = idOld == idNew;
            }catch (Exception e){
                ans = false;
            }
            return ans;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if(oldMetric != newMetric) return false;
            try{
                Land oldLand = oldData.get(oldItemPosition);
                Land newLand = newData.get(newItemPosition);
                return oldLand.getData().toString().equals(newLand.getData().toString()) && oldLand.isSelected() == newLand.isSelected();
            }catch (Exception ignore){}
            return false;
        }
    }
}
