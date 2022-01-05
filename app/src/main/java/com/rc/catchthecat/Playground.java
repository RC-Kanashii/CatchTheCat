package com.rc.catchthecat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class Playground extends SurfaceView {

    public Playground(Context context) {
        super(context);
        // 传递回调类
        getHolder().addCallback(callback);
    }

    private void redraw(){
        // 获取Canvas对象
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.CYAN);
        // 将画布内容进行提交
        getHolder().unlockCanvasAndPost(canvas);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
            // 第一次显示时，把内容显示在屏幕上
            redraw();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

        }
    };
}
