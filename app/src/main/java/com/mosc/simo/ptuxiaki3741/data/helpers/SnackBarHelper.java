package com.mosc.simo.ptuxiaki3741.data.helpers;

import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;

public class SnackBarHelper {
    public static NoSwipeBehavior SnackBarNoSwipeBehavior(){
        return new NoSwipeBehavior();
    }
    public static class NoSwipeBehavior extends BaseTransientBottomBar.Behavior {
        @Override
        public boolean canSwipeDismissView(View child) {
            return false;
        }
    }
}
