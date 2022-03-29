package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandZoneData;
import com.mosc.simo.ptuxiaki3741.data.models.LandZone;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderBulkEditZoneBinding;

import java.util.ArrayList;
import java.util.List;

public class ZonesBulkEditorAdapter extends RecyclerView.Adapter<ZonesBulkEditorAdapter.ViewHolder>{
    private final List<LandZone> data = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.getNewInstance(LayoutInflater.from(parent.getContext()),parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position < 0 || position >= data.size()) return;
        holder.setData(data.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void saveData(List<LandZone> data){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ZonesBulkEditorAdapter.ZonesBulkEditDiffUtil(this.data, data));
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewHolderBulkEditZoneBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderBulkEditZoneBinding.bind(itemView);
            binding.tvTitle.setSelected(true);
            binding.tvNote.setSelected(true);
            binding.tvTags.setSelected(true);
            binding.tvColor.setSelected(true);
        }
        public void setData(LandZoneData data){
            if(data == null) return;
            binding.tvTitle.setText(data.toString());
            binding.tvNote.setText(data.getNote());
            binding.tvTags.setText(data.getTags());
            binding.tvColor.setBackgroundColor(data.getColor().getColor());
        }
        public static ViewHolder getNewInstance(LayoutInflater inflater, ViewGroup parent){
            ViewHolderBulkEditZoneBinding binding = ViewHolderBulkEditZoneBinding.inflate(inflater,parent,false);
            return new ViewHolder(binding.getRoot());
        }
    }

    private static class ZonesBulkEditDiffUtil extends DiffUtil.Callback {
        private final List<LandZone> oldData;
        private final List<LandZone> newData;

        public ZonesBulkEditDiffUtil(List<LandZone> oldData, List<LandZone> newData){
            this.oldData = oldData;
            this.newData = newData;
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
            boolean ans1, ans2, ans3, ans4;
            try{
                LandZoneData oldLand = oldData.get(oldItemPosition).getData();
                LandZoneData newLand = newData.get(newItemPosition).getData();
                ans1 = oldLand.getTitle().equals(newLand.getTitle());
                ans2 = oldLand.getNote().equals(newLand.getNote());
                ans3 = oldLand.getTags().equals(newLand.getTags());
                ans4 = oldLand.getColor().equals(newLand.getColor());
            }catch (Exception e){
                ans1 = false;
                ans2 = false;
                ans3 = false;
                ans4 = false;
            }
            return ans1 && ans2 && ans3 && ans4;
        }
    }
}
