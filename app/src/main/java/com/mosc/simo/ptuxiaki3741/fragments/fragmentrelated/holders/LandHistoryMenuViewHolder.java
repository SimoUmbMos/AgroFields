package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.View;

import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter.LandHistoryListAdapter;

import java.util.List;

public class LandHistoryMenuViewHolder {
    private final LandHistoryListAdapter adapter;
    public LandHistoryMenuViewHolder(View view,
                                     List<Object> data,
                                     LandHistoryListAdapter.OnItemClick onLandClick,
                                     LandHistoryListAdapter.OnItemLongClick onLandLongClick) {
        adapter = new LandHistoryListAdapter(data,onLandClick,onLandLongClick);
    }

    public void update() {
        adapter.notifyDataSetChanged();
    }

    public void isLoading(boolean isLoading) {
        //todo is Loading
    }
}
