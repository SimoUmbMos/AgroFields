package com.mosc.simo.ptuxiaki3741.util.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.mosc.simo.ptuxiaki3741.interfaces.OnFragmentTouchEvent;

public class TouchableWrapper extends FrameLayout {
    private OnFragmentTouchEvent onFragmentTouchEvent = new OnFragmentTouchEvent(){};

    public TouchableWrapper(@NonNull Context context) {
        super(context);
    }

    public void setOnFragmentTouchEvent(OnFragmentTouchEvent onFragmentTouchEvent){
        this.onFragmentTouchEvent = onFragmentTouchEvent;
    }

    public void unsetOnFragmentTouchEvent(){
        this.onFragmentTouchEvent = new OnFragmentTouchEvent(){};
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onFragmentTouchEvent.onActionDown(ev);
                break;

            case MotionEvent.ACTION_UP:
                onFragmentTouchEvent.onActionUp(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                onFragmentTouchEvent.onActionMove(ev);
                break;
        }

        return super.dispatchTouchEvent(ev);
    }
}