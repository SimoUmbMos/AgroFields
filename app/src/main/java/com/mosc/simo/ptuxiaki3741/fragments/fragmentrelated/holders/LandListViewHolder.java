package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter.LandListAdapter;
import com.mosc.simo.ptuxiaki3741.backend.viewmodels.LandViewModel;

public class LandListViewHolder {
    private final LandListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyRv, loadingRv;
    private boolean isLoading,showRecyclerView;
    public LandListViewHolder(View view, LandViewModel vmLands,
                              LandListAdapter.OnLandClick onLandClick,
                              LandListAdapter.OnLandLongClick onLandLongClick) {
        isLoading = false;
        showRecyclerView = vmLands.getLandsList().size() > 0;
        adapter = new LandListAdapter(vmLands, onLandClick, onLandLongClick);
        initRecyclerView(view);
    }

    //todo change if possible
    @SuppressLint("NotifyDataSetChanged")
    public void notifyItemsChanged() {
        adapter.notifyDataSetChanged();
    }

    private void initRecyclerView(View view) {
        loadingRv = view.findViewById(R.id.loadingRv);
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

    public void showRc(boolean showRecyclerView) {
        this.showRecyclerView = showRecyclerView;
        if(isLoading) {
            emptyRv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            loadingRv.setVisibility(View.VISIBLE);
        }else if(showRecyclerView){
            loadingRv.setVisibility(View.GONE);
            emptyRv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }else{
            loadingRv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            emptyRv.setVisibility(View.VISIBLE);
        }
    }

    public void setIsLoading(Boolean isLoading) {
        this.isLoading = isLoading;
        showRc(showRecyclerView);
    }
}
