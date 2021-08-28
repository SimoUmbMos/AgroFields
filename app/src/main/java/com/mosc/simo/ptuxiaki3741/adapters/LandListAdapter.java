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

import java.util.List;


public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandListAdapterViewHolder>{

    private final List<Land> data;
    private final List<Integer> indexes;
    private final OnLandClick onLandClick;
    private final OnLandLongClick onLandLongClick;

    public LandListAdapter(List<Land> data, List<Integer> indexes, OnLandClick onLandClick,
                           OnLandLongClick onLandLongClick){
        this.data = data;
        this.indexes = indexes;
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
        holder.binding.llContainer.setOnClickListener(v -> onLandClick.onLandClick(position));
        holder.binding.llContainer.setOnLongClickListener(v -> {
            onLandLongClick.onLandLongClick(position);
            return true;
        });
        if(data.size()>position){
            LandData landData = data.get(position).getData();
            if(landData != null){
                holder.binding.tvLandTitle.setText(landData.getTitle());
            }
            if(indexes.contains(position)){
                holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
            }else{
                holder.binding.ivCheckBox.setVisibility(View.GONE);
            }
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
        void onLandClick(int pos);
    }
    public interface OnLandLongClick{
        void onLandLongClick(int pos);
    }
    protected static class LandListAdapterViewHolder  extends RecyclerView.ViewHolder {
        public final ViewLandListBinding binding;
        public LandListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ViewLandListBinding.bind(itemView);
        }
    }
}
