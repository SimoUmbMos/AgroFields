package com.mosc.simo.ptuxiaki3741.fragments.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class MapImgController {
    private static final int animationDuration = 0;
    private static final float stepOpacity = 0.03f,
            stepRotate = 1f,
            stepZoom = 0.03f,
            maxZoom = 7f,
            minZoom = 0.1f;

    public static final int FlagDisable = 0,
            FlagMove = 1,
            FlagZoom = 2,
            FlagRotate = 3,
            FlagAlpha = 4;

    private final ImageView imageView;

    private float zoom = 1f;
    private float dx, dy, x, y;
    private int flag;
    private boolean isImgVisible=false;

    public MapImgController(ImageView imageView){
        flag = FlagDisable;
        this.imageView = imageView;
    }

    public static boolean isCorrectFlag(int imgTouchActionFlag){
        return imgTouchActionFlag == FlagDisable ||
                imgTouchActionFlag == FlagAlpha ||
                imgTouchActionFlag == FlagMove ||
                imgTouchActionFlag == FlagZoom ||
                imgTouchActionFlag == FlagRotate;
    }

    public void setFlag(int imgTouchActionFlag){
        if(isCorrectFlag(imgTouchActionFlag)){
            this.flag = imgTouchActionFlag;
        }else{
            this.flag = FlagDisable;
        }
    }
    public int getFlag(){
        return flag;
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
        switch (flag){
            case(FlagMove):
                imageView.animate()
                        .translationX(0)
                        .translationY(0)
                        .setDuration(animationDuration).start();
                break;
            case(FlagZoom):
                zoom=1f;
                imageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(animationDuration).start();
                break;
            case(FlagRotate):
                imageView.animate()
                        .rotation(0)
                        .setDuration(animationDuration).start();
                break;
            case(FlagAlpha):
                imageView.animate()
                        .alpha(0.5f)
                        .setDuration(animationDuration).start();
                break;
            case(FlagDisable):
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
            switch (flag){
                case (FlagZoom):
                    zoomImg(isHigher);
                    break;
                case (FlagRotate):
                    rotateImg(isHigher);
                    break;
                case (FlagAlpha):
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
        if( flag == FlagMove && isImgVisible){
            moveImg(x, y);
        }
        initImgValues(ev);
    }
    public void initImgTouch(MotionEvent ev) {
        if( flag == FlagMove ){
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

    public void showImage(File file){
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap selectDrawable = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            imageView.setImageBitmap(selectDrawable);
        }
    }
    public void showImage(Uri uri){
        imageView.setImageURI(uri);
    }
}
