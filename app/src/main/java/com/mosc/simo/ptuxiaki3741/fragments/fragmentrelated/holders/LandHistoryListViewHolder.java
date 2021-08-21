package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.adapter.LandHistoryListAdapter;
import com.mosc.simo.ptuxiaki3741.models.LandHistoryList;

import java.util.List;

public class LandHistoryListViewHolder {
    private final LandHistoryListAdapter adapter;
    private final RecyclerView recyclerView;
    private final TextView tvEmpty;
    private final Resources resources;
    private boolean isLoading;
    public LandHistoryListViewHolder(
            View view,
            List<LandHistoryList> data,
            Resources res
    ){
        isLoading = false;
        resources = res;
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvEmpty.setText(resources.getString(R.string.empty_list));

        recyclerView = view.findViewById(R.id.rvHistoryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                view.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new LandHistoryListAdapter(data);
        recyclerView.setAdapter(adapter);
    }

    public void isLoading(boolean loading) {
        isLoading = loading;
        if(isLoading){
            tvEmpty.setText(resources.getString(R.string.loading_list));
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            tvEmpty.setText(resources.getString(R.string.empty_list));
            checkIfAdapterIsPopulated();
        }
    }

    private void checkIfAdapterIsPopulated() {
        if(!isLoading){
            if(adapter.getItemCount()>0){
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            }else{
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    public void update() {
        adapter.notifyDataSetChanged();
        checkIfAdapterIsPopulated();
    }

    public boolean areAllListsVisible(){
        for(int i = 0; i < adapter.getItemCount(); i++){
            LandHistoryListAdapter.ItemViewHolder vh = (LandHistoryListAdapter.ItemViewHolder)
                    recyclerView.findViewHolderForAdapterPosition(i);
            if(vh != null){
                if(vh.llHistoryRoot.getVisibility() == View.GONE){
                    return false;
                }
            }
        }
        return true;
    }
    public void showAllLists(){
        for(int i = 0; i < adapter.getItemCount(); i++){
            LandHistoryListAdapter.ItemViewHolder vh = (LandHistoryListAdapter.ItemViewHolder)
                    recyclerView.findViewHolderForAdapterPosition(i);
            if(vh != null){
                if(vh.llHistoryRoot.getVisibility() != View.VISIBLE){
                    vh.toggleList();
                }
            }
        }
    }
    public void hideAllLists(){
        for(int i = 0; i < adapter.getItemCount(); i++){
            LandHistoryListAdapter.ItemViewHolder vh = (LandHistoryListAdapter.ItemViewHolder)
                    recyclerView.findViewHolderForAdapterPosition(i);
            if(vh != null){
                if(vh.llHistoryRoot.getVisibility() != View.GONE){
                    vh.toggleList();
                }
            }
        }
    }
}
