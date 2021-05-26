package com.mosc.simo.ptuxiaki3741.interfaces;

import android.view.MotionEvent;

public interface OnFragmentTouchEvent {
    default void onActionUp(MotionEvent ev) {}
    default void onActionDown(MotionEvent ev) {}
    default void onActionMove(MotionEvent ev) {}
}
