package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;

public class LandListRecycleViewHolder {
    private final LandListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyRv;
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
        emptyRv = view.findViewById(R.id.emptyRv);
        recyclerView = view.findViewById(R.id.rvLandList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                        view.getContext(),
                        LinearLayoutManager.VERTICAL,
                        false
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        showRc(adapter.getItemCount() > 0);
    }

    public void showRc(boolean b) {
        if(b){
            emptyRv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }else{
            emptyRv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}
