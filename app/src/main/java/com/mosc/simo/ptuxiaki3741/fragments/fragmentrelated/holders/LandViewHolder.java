package com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.holders;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.fragments.fragmentrelated.controllers.LandImgController;
import com.mosc.simo.ptuxiaki3741.enums.LandImgState;
import com.mosc.simo.ptuxiaki3741.interfaces.OnAction;

public class LandViewHolder implements View.OnTouchListener {
    public final ActionBar actionBar;
    private final LandImgController imgController;

    public final DrawerLayout drawer;
    public final NavigationView navDrawer;
    public final ConstraintLayout clImgTab;
    public final TextView tvImgAction;
    public final ImageView imageView;
    public final FrameLayout touchLayer;
    public final FloatingActionButton fabSave,fabReset,fabPlus,fabMinus;
    public MenuItem miLock;

    private OnAction onClose;

    @SuppressLint("ClickableViewAccessibility")
    public LandViewHolder(View view, ActionBar actionBar, LandImgController imgController) {
        this.imgController = imgController;
        this.actionBar = actionBar;

        touchLayer = view.findViewById(R.id.map_touch_layer);
        clImgTab = view.findViewById(R.id.clImgTab);
        tvImgAction = view.findViewById(R.id.tvImgAction);
        fabSave = view.findViewById(R.id.fabSave);
        fabReset = view.findViewById(R.id.fabReset);
        fabMinus = view.findViewById(R.id.fabMinus);
        fabPlus = view.findViewById(R.id.fabPlus);
        imageView = view.findViewById(R.id.imageView);
        drawer = view.findViewById(R.id.drawer_layout);
        navDrawer = view.findViewById(R.id.nav_view);

        miLock = navDrawer.getMenu().findItem(R.id.toolbar_action_toggle_map_lock);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, navDrawer);

        onClose= new OnAction(){};

        touchLayer.setVisibility(View.GONE);
        touchLayer.setOnTouchListener(this);
    }
    public void openDrawer(){
        drawer.openDrawer(GravityCompat.END);
    }
    public void closeDrawer(){
        drawer.closeDrawer(GravityCompat.END);
    }
    public void showTabMenu(
            String title,
            View.OnClickListener btnSave,
            View.OnClickListener btnCancel
    ){
        clImgTab.setVisibility(View.VISIBLE);
        fabPlus.setVisibility(View.GONE);
        fabMinus.setVisibility(View.GONE);
        fabSave.setOnClickListener(btnSave);
        fabReset.setOnClickListener(btnCancel);
        tvImgAction.setText(title);
    }
    public void showTabMenu(
            String title,
            View.OnClickListener btnPlus,
            View.OnClickListener btnMinus,
            View.OnClickListener btnSave,
            View.OnClickListener btnCancel
    ){
        clImgTab.setVisibility(View.VISIBLE);
        fabPlus.setVisibility(View.VISIBLE);
        fabMinus.setVisibility(View.VISIBLE);
        fabPlus.setOnClickListener(btnPlus);
        fabMinus.setOnClickListener(btnMinus);
        fabSave.setOnClickListener(btnSave);
        fabReset.setOnClickListener(btnCancel);
        tvImgAction.setText(title);
    }
    public void closeTabMenu(){
        clImgTab.setVisibility(View.GONE);
        touchLayer.setVisibility(View.GONE);
        fabPlus.setOnClickListener(null);
        fabMinus.setOnClickListener(null);
        fabSave.setOnClickListener(null);
        fabReset.setOnClickListener(null);
        tvImgAction.setText("");
        onClose.onCloseTab();
    }
    public void hideImgView() {
        imageView.setVisibility(View.GONE);
    }

    public void changeTabMenuTitle(String title) {
        tvImgAction.setText(title);
    }

    public void setOnClose(OnAction onClose) {
        this.onClose = onClose;
    }

    public void addNewOverlayImg() {
        if(imgController != null){
            imgController.addNewOverlayImg();
        }
    }
    public void removeOverlayImg() {
        touchLayer.setVisibility(View.GONE);
        if(imgController != null){
            imgController.removeOverlayImg();
        }
    }
    public void showImage(Uri uri) {
        if(imgController != null){
            imgController.showImage(uri);
        }
    }

    public void save(OnAction action) {
        closeTabMenu();
        clearFlag();
        action.onSave();
    }
    public void undo(OnAction action) {
        action.onUndo();
    }

    public void save() {
        closeTabMenu();
        clearFlag();
    }

    private void clearFlag() {
        if(imgController != null){
            imgController.setFlag(LandImgState.Disable);
        }
    }

    public void onImgActionClick(LandImgState imgState){
        if(imgController != null){
            if (imgController.getImgVisible()) {
                imgController.setFlag(imgState);
                switch (imgState){
                    case Alpha:
                        setupAlphaAction();
                        break;
                    case Move:
                        setupMoveAction();
                        break;
                    case Rotate:
                        setupRotateAction();
                        break;
                    case Zoom:
                        setupZoomAction();
                        break;
                }
            }
        }
    }

    private void setupMoveAction() {
        if(imageView.getVisibility() == View.VISIBLE){
            touchLayer.setVisibility(View.VISIBLE);
        }else{
            touchLayer.setVisibility(View.GONE);
        }
        showTabMenu(
                "Move",
                save->save(),
                undo->undo(new OnAction() {
                    @Override
                    public void onUndo() {
                        if(imgController != null){
                            imgController.resetImg();
                        }
                    }
                })
        );

    }
    private void setupZoomAction() {
        if(imgController != null){
            showTabMenu(
                    "Zoom",
                    plus->imgController.doAction(true),
                    minus->imgController.doAction(false),
                    save->save(),
                    undo->undo(new OnAction() {
                        @Override
                        public void onUndo() {
                            imgController.resetImg();
                        }
                    })
            );
        }
    }
    private void setupRotateAction() {
        if(imgController != null){
            showTabMenu(
                "Rotate",
                plus->imgController.doAction(false),
                minus->imgController.doAction(true),
                save->save(),
                undo->undo(new OnAction() {
                    @Override
                    public void onUndo() {
                        imgController.resetImg();
                    }
                })
            );
        }
    }
    private void setupAlphaAction() {
        if(imgController != null){
            showTabMenu(
                "Alpha",
                plus->imgController.doAction(true),
                minus->imgController.doAction(false),
                save->save(),
                undo->undo(new OnAction() {
                    @Override
                    public void onUndo() {
                        imgController.resetImg();
                    }
                })
            );
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(imgController != null){
            if(imageView.getVisibility() == View.VISIBLE){
                Log.d("debug", "onTouch: "+debugMotionEvent(event));
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1) {
                            imgController.imgTouchMove(event);
                        }else{
                            v.performClick();
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() == 1) {
                            imgController.initImgTouch(event);
                        }else{
                            v.performClick();
                        }
                        break;
                    default:
                        v.performClick();
                        break;
                }
            }
        }
        return true;
    }

    private String debugMotionEvent(MotionEvent event) {
        String ans;
        if (event.getPointerCount() > 1) {
            ans="Multitouch event ";
        } else {
            ans="Single touch event ";
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: return ans+"Down";
            case MotionEvent.ACTION_MOVE: return ans+"Move";
            case MotionEvent.ACTION_POINTER_DOWN: return ans+"Pointer Down";
            case MotionEvent.ACTION_UP: return ans+"Up";
            case MotionEvent.ACTION_POINTER_UP: return ans+"Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return ans+"Outside";
            case MotionEvent.ACTION_CANCEL: return ans+"Cancel";
        }
        return "";
    }

    public void onActivityResult(Intent result) {
        if(result != null){
            Uri uri = result.getData();
            addNewOverlayImg();
            showImage(uri);
        }
    }

    public String getTitle() {
        String title = "";
        if(actionBar != null){
            if(actionBar.getTitle() != null){
                title = actionBar.getTitle().toString();
            }
        }
        return title.trim();
    }

    public void setTitle(String title) {
        if(actionBar != null){
            actionBar.setTitle(title);
        }
    }
}
