package com.mosc.simo.ptuxiaki3741.ui.recycler_view_adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.data.models.CalendarCategoryEntity;
import com.mosc.simo.ptuxiaki3741.data.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.data.models.ColorData;
import com.mosc.simo.ptuxiaki3741.data.util.UIUtil;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderNotificationCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class CalendarCategoriesAdapter extends RecyclerView.Adapter<CalendarCategoriesAdapter.CalendarCategoryViewHolder>{
    private final List<CalendarCategoryEntity> data;
    private boolean showCheckBox;
    private final ActionResult<CalendarCategoryEntity> onPositionClick;
    private final ActionResult<CalendarCategoryEntity> onPositionLongClick;

    public CalendarCategoriesAdapter(
            ActionResult<CalendarCategoryEntity> onPositionClick,
            ActionResult<CalendarCategoryEntity> onPositionLongClick
    ) {
        data = new ArrayList<>();
        showCheckBox = false;
        this.onPositionClick = onPositionClick;
        this.onPositionLongClick = onPositionLongClick;
    }

    public void saveData(List<CalendarCategoryEntity> newData) {
        List<CalendarCategoryEntity> oldData = new ArrayList<>(data);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtilCallback(oldData,newData));
        data.clear();
        data.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    public void saveData(List<CalendarCategoryEntity> newData, boolean showCheckBox) {
        List<CalendarCategoryEntity> oldData = new ArrayList<>(data);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtilCallback(oldData,newData,this.showCheckBox,showCheckBox));
        this.showCheckBox = showCheckBox;
        data.clear();
        data.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CalendarCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CalendarCategoryViewHolder.getInstance(
                LayoutInflater.from(parent.getContext()),
                parent
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarCategoryViewHolder holder, int position) {
        if(position < 0 || position >= data.size()) return;

        CalendarCategoryEntity entity = data.get(position);
        holder.setData(entity,showCheckBox);
        if(onPositionClick != null) {
            holder.binding.mcvBackgroundCard.setOnClickListener(v -> onPositionClick.onActionResult(entity));
        }
        if(onPositionLongClick != null) {
            holder.binding.mcvBackgroundCard.setOnLongClickListener(v -> {
                onPositionLongClick.onActionResult(entity);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    protected static class CalendarCategoryViewHolder extends RecyclerView.ViewHolder {
        public final ViewHolderNotificationCategoryBinding binding;
        public CalendarCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderNotificationCategoryBinding.bind(itemView);
        }

        public void setData(CalendarCategoryEntity data, boolean showCheckbox){
            if(data == null || data.getData() == null) return;
            binding.tvTitle.setText(data.getData().getName());
            binding.tvTitle.setSelected(true);
            binding.cbSelect.setChecked(data.isSelected());
            if(showCheckbox){
                binding.cbSelect.setVisibility(View.VISIBLE);
            }else{
                binding.cbSelect.setVisibility(View.GONE);
            }
            ColorData color = data.getData().getColorData();
            if(color != null){
                binding.mcvBackgroundCard.setCardBackgroundColor(color.getColor());
                if(UIUtil.showBlackText(color)){
                    binding.tvTitle.setTextColor(Color.BLACK);
                    binding.cbSelect.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
                }else{
                    binding.tvTitle.setTextColor(Color.WHITE);
                    binding.cbSelect.setButtonTintList(ColorStateList.valueOf(Color.WHITE));
                }
            }
        }

        public static CalendarCategoryViewHolder getInstance(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent){
            ViewHolderNotificationCategoryBinding binding = ViewHolderNotificationCategoryBinding.inflate(inflater,parent,false);
            return new CalendarCategoryViewHolder(binding.getRoot());
        }
    }

    private static class DiffUtilCallback extends DiffUtil.Callback {
        private final List<CalendarCategoryEntity> oldData, newData;
        private final boolean oldCB, newCB;

        public DiffUtilCallback(List<CalendarCategoryEntity> oldData, List<CalendarCategoryEntity> newData) {
            this.oldData = oldData;
            this.newData = newData;
            this.oldCB = false;
            this.newCB = false;
        }

        public DiffUtilCallback(List<CalendarCategoryEntity> oldData, List<CalendarCategoryEntity> newData, boolean oldCB, boolean newCB) {
            this.oldData = oldData;
            this.newData = newData;
            this.oldCB = oldCB;
            this.newCB = newCB;
        }

        @Override
        public int getOldListSize() {
            try{
                return oldData.size();
            }catch (Exception ignore){
                return 0;
            }
        }

        @Override
        public int getNewListSize() {
            try{
                return newData.size();
            }catch (Exception ignore){
                return 0;
            }
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            try{
                return oldData.get(oldItemPosition).getData().getId() == newData.get(newItemPosition).getData().getId();
            }catch (Exception ignore){
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if(oldCB != newCB) return false;
            try{
                CalendarCategoryEntity oldItem = oldData.get(oldItemPosition);
                CalendarCategoryEntity newItem = newData.get(newItemPosition);
                if(!oldItem.getData().getName().equals(newItem.getData().getName())) return false;
                return oldItem.getData().getColorData().equals(newItem.getData().getColorData());
            }catch (Exception ignore){
                return false;
            }
        }
    }
}
