package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.backend.room.entities.LandData;
import com.mosc.simo.ptuxiaki3741.data.models.Land;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderBulkEditLandBinding;

import java.util.ArrayList;
import java.util.List;

public class LandsBulkEditorAdapter extends RecyclerView.Adapter<LandsBulkEditorAdapter.ViewHolder>{
    private final List<Land> data;

    public LandsBulkEditorAdapter(){
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.getNewInstance(
                LayoutInflater.from(parent.getContext()),
                parent
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Land land = data.get(position);
        holder.setData(land.getData());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void saveData(List<Land> data){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LandsBulkEditDiffUtil(this.data, data));
        this.data.clear();
        this.data.addAll(data);
        diffResult.dispatchUpdatesTo(this);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewHolderBulkEditLandBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderBulkEditLandBinding.bind(itemView);
            binding.tvTitle.setSelected(true);
            binding.tvTags.setSelected(true);
            binding.tvColor.setSelected(true);
        }
        public void setData(LandData data){
            if(data == null) return;
            binding.tvTitle.setText(data.toString());
            binding.tvTags.setText(data.getTags());
            binding.tvColor.setBackgroundColor(data.getColor().getColor());
        }
        public static ViewHolder getNewInstance(LayoutInflater inflater, ViewGroup parent){
            ViewHolderBulkEditLandBinding binding = ViewHolderBulkEditLandBinding.inflate(inflater,parent,false);
            return new ViewHolder(binding.getRoot());
        }
    }

    private static class LandsBulkEditDiffUtil extends DiffUtil.Callback {
        private final List<Land> oldData;
        private final List<Land> newData;

        public LandsBulkEditDiffUtil(List<Land> oldData, List<Land> newData){
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
            boolean ans1, ans2, ans3;
            try{
                LandData oldLand = oldData.get(oldItemPosition).getData();
                LandData newLand = newData.get(newItemPosition).getData();
                ans1 = oldLand.getTitle().equals(newLand.getTitle());
                ans2 = oldLand.getTags().equals(newLand.getTags());
                ans3 = oldLand.getColor().equals(newLand.getColor());
            }catch (Exception e){
                ans1 = false;
                ans2 = false;
                ans3 = false;
            }
            return ans1 && ans2 && ans3;
        }
    }
}
