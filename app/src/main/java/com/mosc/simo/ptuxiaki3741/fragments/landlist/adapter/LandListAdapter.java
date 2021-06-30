package com.mosc.simo.ptuxiaki3741.fragments.landlist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;

import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandListAdapterViewHolder>{

    private final LiveData<List<Land>> lands;
    private final LiveData<List<Integer>> selectedLands;
    private final OnLandClick onLandClick;
    private final OnLandLongClick onLandLongClick;

    public LandListAdapter(LiveData<List<Land>> lands,
                           LiveData<List<Integer>> selectedLands, OnLandClick onLandClick,
                           OnLandLongClick onLandLongClick){
        this.lands = lands;
        this.selectedLands = selectedLands;
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
        List<Land> mLands = lands.getValue();
        List<Integer> mSelectedLands = selectedLands.getValue();
        if( mSelectedLands != null && mLands != null){
            holder.tvLandTitle.setText(mLands.get(position).getTitle());
            if(mSelectedLands.contains(position)){
                holder.ivCheckBox.setVisibility(View.VISIBLE);
            }else{
                holder.ivCheckBox.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        List<Land> mLands = lands.getValue();
        if(mLands != null)
            return mLands.size();
        else
            return 0;
    }

    public interface OnLandClick{
        void onLandClick(int pos);
    }
    public interface OnLandLongClick{
        boolean onLandLongClick(int pos);
    }
    protected static class LandListAdapterViewHolder  extends RecyclerView.ViewHolder {
        public LinearLayout llContainer;
        public TextView tvLandTitle;
        public ImageView ivCheckBox;
        public LandListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            llContainer = itemView.findViewById(R.id.llContainer);
            tvLandTitle = itemView.findViewById(R.id.tvLandTitle);
            ivCheckBox = itemView.findViewById(R.id.ivCheckBox);
        }
    }
}
