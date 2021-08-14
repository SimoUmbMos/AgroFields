package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandRecord;

import java.util.List;

public class LandHistoryListAdapter
        extends RecyclerView.Adapter<LandHistoryListAdapter.LandHistoryListAdapterViewHolder>{
    private final LandViewModel viewModel;
    private final OnLandClick onLandClick;
    private final OnLandLongClick onLandLongClick;

    public LandHistoryListAdapter(
            LandViewModel landViewModel,
            OnLandClick onLandClick,
            OnLandLongClick onLandLongClick
    ){
        this.viewModel = landViewModel;
        this.onLandClick = onLandClick;
        this.onLandLongClick = onLandLongClick;
    }

    @NonNull
    @Override
    public LandHistoryListAdapterViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_land_history_list,parent,false);
        return new LandHistoryListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LandHistoryListAdapterViewHolder holder,
            int position
    ) {
        holder.root.setOnClickListener(v->onLandClick.onLandClick(position));
        holder.root.setOnLongClickListener(v->onLandLongClick.onLandLongClick(position));
        if(viewModel.getLandsHistoryList().size() > position){
            LandRecord landRecord = viewModel.getLandsHistoryList().get(position);
            //todo init views on LandHistoryListAdapterViewHolder based landRecord
        }
    }

    @Override
    public int getItemCount() {
        if(viewModel != null)
            return viewModel.getLandsHistoryList().size();
        else
            return 0;
    }

    public interface OnLandClick{
        void onLandClick(int pos);
    }
    public interface OnLandLongClick{
        boolean onLandLongClick(int pos);
    }
    protected static class LandHistoryListAdapterViewHolder extends RecyclerView.ViewHolder {
        public View root;
        public LandHistoryListAdapterViewHolder(@NonNull View root) {
            super(root);
            this.root = root;
            //todo create views for LandHistoryListAdapterViewHolder
        }
    }
}
