package com.mosc.simo.ptuxiaki3741.fragments.landlist.holders;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.adapter.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;

import java.util.List;

public class LandListRecycleViewHolder {

    public RecyclerView recyclerView;
    public ConstraintLayout root;

    private final LandViewModel vmLands;
    private final LandListAdapter adapter;

    public LandListRecycleViewHolder(View view, LandViewModel vmLands,
                                     LandListAdapter.OnLandClick onLandClick,
                                     LandListAdapter.OnLandLongClick onLandLongClick) {
        this.vmLands = vmLands;
        root = view.findViewById(R.id.root);
        recyclerView = view.findViewById(R.id.rvLandList);
        adapter = new LandListAdapter(vmLands.getLands(), vmLands.getSelectedLands(), onLandClick,onLandLongClick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                        view.getContext(),
                        LinearLayoutManager.VERTICAL,
                        false
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }


    public void notifyItemsChanged() {
        adapter.notifyDataSetChanged();
    }
    public void notifyItemInserted(int position) {
        adapter.notifyItemInserted(position);
        adapter.notifyItemRangeChanged(position,vmLands.landSize());
    }
    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position,vmLands.landSize());
    }
}
