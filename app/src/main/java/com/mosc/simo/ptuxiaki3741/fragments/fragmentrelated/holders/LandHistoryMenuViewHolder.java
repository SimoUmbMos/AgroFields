package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.View;

import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter.LandHistoryListAdapter;

public class LandHistoryMenuViewHolder {
    private final LandHistoryListAdapter adapter;
    public LandHistoryMenuViewHolder(View view,
                                     LandViewModel vmLands,
                                     LandHistoryListAdapter.OnLandClick onLandClick,
                                     LandHistoryListAdapter.OnLandLongClick onLandLongClick) {
        adapter = new LandHistoryListAdapter(vmLands,onLandClick,onLandLongClick);
    }

    public void update() {
        adapter.notifyDataSetChanged();
    }
}
