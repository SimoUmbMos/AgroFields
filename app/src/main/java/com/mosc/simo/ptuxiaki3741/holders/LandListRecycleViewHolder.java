package com.mosc.simo.ptuxiaki3741.holders;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.adapter.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.viewmodels.LandViewModel;

public class LandListRecycleViewHolder {
    private final LandListAdapter adapter;

    public LandListRecycleViewHolder(View view, LandViewModel vmLands,
                                     LandListAdapter.OnLandClick onLandClick,
                                     LandListAdapter.OnLandLongClick onLandLongClick) {
        adapter = new LandListAdapter(vmLands, onLandClick, onLandLongClick);
        initRecyclerView(view);
    }

    public void notifyItemsChanged() {
        adapter.notifyDataSetChanged();
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvLandList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                        view.getContext(),
                        LinearLayoutManager.VERTICAL,
                        false
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }
}
