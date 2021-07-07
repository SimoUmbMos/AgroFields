package com.mosc.simo.ptuxiaki3741.fragmentrelated.controllers;

import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.mosc.simo.ptuxiaki3741.enums.LandImgState;

public class LandImgController {
    private static final int animationDuration = 0;
    private static final float stepOpacity = 0.03f,
            stepRotate = 1f,
            stepZoom = 0.03f,
            maxZoom = 7f,
            minZoom = 0.1f;

    private final ImageView imageView;

    private float zoom = 1f;
    private float dx, dy, x, y;
    private LandImgState state;
    private boolean isImgVisible=false;

    public LandImgController(ImageView imageView){
        state = LandImgState.Disable;
        this.imageView = imageView;
    }

    public void setFlag(LandImgState state){
        this.state = state;
    }
    public boolean getImgVisible(){
        return isImgVisible;
    }

    public void moveImg(float stepX, float stepY){
        imageView.animate()
                .translationX(stepX)
                .translationY(stepY)
                .setDuration(animationDuration).start();
    }
    public void zoomImg(boolean isHigher){
        if(isHigher){
            if((zoom+stepZoom)<maxZoom){
                zoom+=stepZoom;
                imageView.animate()
                        .scaleXBy(stepZoom)
                        .scaleYBy(stepZoom)
                        .setDuration(animationDuration).start();
            }
        }else{
            if((zoom-stepZoom)>minZoom){
                zoom-=stepZoom;
                imageView.animate()
                        .scaleXBy(-stepZoom)
                        .scaleYBy(-stepZoom)
                        .setDuration(animationDuration).start();
            }
        }
    }
    public void rotateImg(boolean isHigher){
        if(isHigher){
            imageView.animate()
                    .rotationBy(stepRotate)
                    .setDuration(animationDuration).start();
        }else{
            imageView.animate()
                    .rotationBy(-stepRotate)
                    .setDuration(animationDuration).start();
        }
    }
    public void opacityImg(boolean isHigher){
        if(isHigher){
            imageView.animate()
                    .alphaBy(stepOpacity)
                    .setDuration(animationDuration).start();
        }else{
            imageView.animate()
                    .alphaBy(-stepOpacity)
                    .setDuration(animationDuration).start();
        }
    }
    public void resetImg(){
        switch (state){
            case Move:
                imageView.animate()
                        .translationX(0)
                        .translationY(0)
                        .setDuration(animationDuration).start();
                break;
            case Zoom:
                zoom=1f;
                imageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(animationDuration).start();
                break;
            case Rotate:
                imageView.animate()
                        .rotation(0)
                        .setDuration(animationDuration).start();
                break;
            case Alpha:
                imageView.animate()
                        .alpha(0.5f)
                        .setDuration(animationDuration).start();
                break;
            case Disable:
                zoom=1f;
                imageView.animate()
                        .alpha(0.5f)
                        .rotation(0)
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationX(0)
                        .translationY(0)
                        .setDuration(animationDuration).start();
                break;
        }
    }

    public void doAction(boolean isHigher) {
        if(isImgVisible){
            switch (state){
                case Zoom:
                    zoomImg(isHigher);
                    break;
                case Rotate:
                    rotateImg(isHigher);
                    break;
                case Alpha:
                    opacityImg(isHigher);
                    break;
            }
        }
    }
    private void initImgValues(MotionEvent ev) {
        dx = ev.getX();
        dy = ev.getY();
        x = imageView.getX();
        y = imageView.getY();
    }
    public void imgTouchMove(MotionEvent ev) {
        float h = dx - ev.getX();
        float v = dy - ev.getY();
        x -= h;
        y -= v;
        if( state == LandImgState.Move && isImgVisible){
            moveImg(x, y);
        }
        initImgValues(ev);
    }
    public void initImgTouch(MotionEvent ev) {
        if( state == LandImgState.Move ){
            initImgValues(ev);
        }
    }

    public void addNewOverlayImg() {
        resetImg();
        isImgVisible=true;
        imageView.setVisibility(View.VISIBLE);
    }

    public void removeOverlayImg() {
        resetImg();
        isImgVisible=false;
        imageView.setVisibility(View.GONE);
    }

    public void showImage(Uri uri){
        imageView.setImageURI(uri);
    }
}
