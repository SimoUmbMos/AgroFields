package com.mosc.simo.ptuxiaki3741.fragmentrelated.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;

import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandListAdapterViewHolder>{

    private final LandViewModel landViewModel;
    private final OnLandClick onLandClick;
    private final OnLandLongClick onLandLongClick;

    public LandListAdapter(LandViewModel landViewModel, OnLandClick onLandClick,
                           OnLandLongClick onLandLongClick){
        this.landViewModel = landViewModel;
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
        holder.llContainer.setOnClickListener(v -> onLandClick.onLandClick(position));
        holder.llContainer.setOnLongClickListener(v -> onLandLongClick.onLandLongClick(position));
        List<Land> mLands = landViewModel.getLandsList();
        List<Integer> mSelectedLands = landViewModel.getSelectedIndexes();
        if( mSelectedLands != null && mLands != null){
            if(mLands.get(position).getData() != null){
                holder.tvLandTitle.setText(mLands.get(position).getData().getTitle());
            }
            if(mSelectedLands.contains(position)){
                holder.ivCheckBox.setVisibility(View.VISIBLE);
            }else{
                holder.ivCheckBox.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return landViewModel.landSize();
    }

    public interface OnLandClick{
        void onLandClick(int pos);
    }
    public interface OnLandLongClick{
        boolean onLandLongClick(int pos);
    }
    protected static class LandListAdapterViewHolder  extends RecyclerView.ViewHolder {
        public final LinearLayout llContainer;
        public final TextView tvLandTitle;
        public final ImageView ivCheckBox;
        public LandListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            llContainer = itemView.findViewById(R.id.llContainer);
            tvLandTitle = itemView.findViewById(R.id.tvLandTitle);
            ivCheckBox = itemView.findViewById(R.id.ivCheckBox);
        }
    }
}
