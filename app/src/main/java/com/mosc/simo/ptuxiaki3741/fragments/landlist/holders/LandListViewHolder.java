package com.mosc.simo.ptuxiaki3741.fragments.landlist.holders;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.database.model.Land;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.helpers.LandListMenuState;
import com.mosc.simo.ptuxiaki3741.fragments.landlist.adapter.LandListAdapter;

import java.util.List;

public class LandListViewHolder {
    private final View view;
    private final LandListAdapter adapter;
    private final List<Land> lands;


    public RecyclerView recyclerView;

    public LandListViewHolder(View view, List<Land> lands, List<Integer> selectedLands,
                              LandListAdapter.OnLandClick onLandClick, LandListAdapter.OnLandLongClick onLandLongClick) {
        this.view = view;
        this.lands = lands;
        adapter = new LandListAdapter(lands,selectedLands,onLandClick,onLandLongClick);

        recyclerView = view.findViewById(R.id.rvLandList);
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
        adapter.notifyItemRangeChanged(position,lands.size());
    }
    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position,lands.size());
    }
}
