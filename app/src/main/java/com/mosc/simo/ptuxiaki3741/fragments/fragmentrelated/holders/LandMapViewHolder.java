package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.mosc.simo.ptuxiaki3741.R;

public class LandMapViewHolder {
    public final View root;
    public final DrawerLayout drawer;
    public final NavigationView navDrawer;
    public final ConstraintLayout clTab;
    public final ImageView imageView;
    public final FrameLayout touchLayer;
    public final MenuItem miLock,resetAll;
    public final FloatingActionButton terrainButton, fabSave, fabReset, fabPlus, fabMinus;
    public LandMapViewHolder(View view){
        root = view;
        terrainButton = view.findViewById(R.id.terrainButton);

        touchLayer = view.findViewById(R.id.map_touch_layer);
        clTab = view.findViewById(R.id.clImgTab);
        fabSave = view.findViewById(R.id.fabSave);
        fabReset = view.findViewById(R.id.fabReset);
        fabMinus = view.findViewById(R.id.fabMinus);
        fabPlus = view.findViewById(R.id.fabPlus);
        imageView = view.findViewById(R.id.imageView);
        drawer = view.findViewById(R.id.drawer_layout);
        navDrawer = view.findViewById(R.id.nav_view);
        miLock = navDrawer.getMenu().findItem(R.id.toolbar_action_toggle_map_lock);
        resetAll = navDrawer.getMenu().findItem(R.id.toolbar_action_clean);

        clTab.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        touchLayer.setVisibility(View.GONE);
    }
}
