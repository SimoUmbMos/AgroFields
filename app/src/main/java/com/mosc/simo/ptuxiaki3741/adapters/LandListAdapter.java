package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandBinding;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHolderLandCheckableBinding;
import com.mosc.simo.ptuxiaki3741.interfaces.ActionResult;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int LIST_DEFAULT = 1,LIST_CHECKABLE = 2;
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
    public void setShowCheckMark(
            boolean showCheckMark
    ){
        this.showCheckMark = showCheckMark;
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        if(viewType == LIST_CHECKABLE){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_holder_land_checkable,parent,false);
            return new LandItemCheckable(view);
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_holder_land,parent,false);
            return new LandItem(view);
        }
    }

    @Override public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        if(data.size()>position){
            Land land = data.get(position);
            LandData landData = land.getData();
            if(landData != null){
                String display = landData.getTitle()+" #"+ EncryptUtil.convert4digit(landData.getId());
                if(holder.getItemViewType() == LIST_CHECKABLE){
                    LandItemCheckable item = (LandItemCheckable) holder;
                    item.binding.ctvLandName.setText(display);
                    item.binding.ctvLandName.setChecked(land.isSelected());
                    item.binding.ctvLandName.setOnClickListener(v ->
                            onLandClick.onActionResult(land)
                    );
                    item.binding.ctvLandName.setOnLongClickListener(v -> {
                        onLandLongClick.onActionResult(land);
                        return true;
                    });
                }else{
                    LandItem item = (LandItem) holder;
                    item.binding.ctvLandName.setText(display);
                    item.binding.ctvLandName.setOnClickListener(v ->
                            onLandClick.onActionResult(land)
                    );
                    item.binding.ctvLandName.setOnLongClickListener(v -> {
                        onLandLongClick.onActionResult(land);
                        return true;
                    });
                }
            }
        }
    }

    @Override public int getItemCount() {
        if(data != null)
            return data.size();
        else
            return 0;
    }
    @Override public int getItemViewType(
            int position
    ) {
        if(showCheckMark)
            return LIST_CHECKABLE;
        return LIST_DEFAULT;
    }

    protected static class LandItem extends RecyclerView.ViewHolder {
        public final ViewHolderLandBinding binding;
        public LandItem(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderLandBinding.bind(itemView);
        }
    }
    protected static class LandItemCheckable extends RecyclerView.ViewHolder {
        public final ViewHolderLandCheckableBinding binding;
        public LandItemCheckable(@NonNull View itemView) {
            super(itemView);
            binding = ViewHolderLandCheckableBinding.bind(itemView);
        }
    }
}
