package com.mosc.simo.ptuxiaki3741.fragments.landlist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;

import java.util.List;

public class LandListAdapter extends RecyclerView.Adapter<LandListAdapter.LandListAdapterViewHolder>{

    private final List<Land> lands;
    private final List<Integer> selectedLands;
    private final OnLandClick onLandClick;
    private final OnLandLongClick onLandLongClick;

    public LandListAdapter(List<Land> lands,
                           List<Integer> selectedLands,
                           OnLandClick onLandClick,
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
        holder.tvLandTitle.setText(lands.get(position).getTitle());
        if(selectedLands.contains(position)){
            holder.ivCheckBox.setVisibility(View.VISIBLE);
        }else{
            holder.ivCheckBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lands.size();
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
