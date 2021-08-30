package com.mosc.simo.ptuxiaki3741.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewLandListBinding;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.List;


public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandListAdapterViewHolder>{

    private final List<Land> data;
    private final OnLandClick onLandClick;
    private final OnLandLongClick onLandLongClick;

    public LandListAdapter(List<Land> data, OnLandClick onLandClick,
                           OnLandLongClick onLandLongClick){
        this.data = data;
        this.onLandClick = onLandClick;
        this.onLandLongClick = onLandLongClick;
    }

    @NonNull
    @Override
    public LandListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_land_list,parent,false);
        return new LandListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LandListAdapterViewHolder holder, int position) {
        if(data.size()>position){
            Land land = data.get(position);
            LandData landData = land.getData();
            if(landData != null){
                String display = landData.getTitle()+" #"+ EncryptUtil.convert4digit(landData.getId());
                holder.binding.tvLandTitle.setText(display);
            }
            if(!land.isSelected()){
                holder.binding.tvLandTitle.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            }
            holder.binding.llContainer.setOnClickListener(v ->
                    onLandClick.onLandClick(land)
            );
            holder.binding.llContainer.setOnLongClickListener(v -> {
                onLandLongClick.onLandLongClick(land);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        else
            return 0;
    }

    public interface OnLandClick{
        void onLandClick(Land land);
    }
    public interface OnLandLongClick{
        void onLandLongClick(Land land);
    }
    protected static class LandListAdapterViewHolder  extends RecyclerView.ViewHolder {
        public final ViewLandListBinding binding;
        public LandListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewLandListBinding.bind(itemView);
        }
    }
}
