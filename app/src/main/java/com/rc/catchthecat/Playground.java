package com.rc.catchthecat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.Random;

public class Playground extends SurfaceView {
    // 行数
    private final int ROW = 10;
    // 列数
    private final int COL = 10;
    // // 默认添加的路障数量
    private final int NUM_BARRIERS = 10;
    // 地图
    private Dot[][] map;
    // 猫
    private Dot cat;
    // 随机数
    private Random random;

    public Playground(Context context) {
        super(context);
        // 传递回调类
        getHolder().addCallback(callback);
        random = new Random();
        initMap();
        initGame();
    }

    private Dot getDot(int i, int j) {
        // 注意：游戏中行列坐标和此处的i, j是反着的
        return map[j][i];
    }

    private void initMap() {
        map = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                // 注意：游戏中行列坐标和此处的i, j是反着的
                map[i][j] = new Dot(j, i);
            }
        }
    }

    private void initGame() {
        // 地图中所有点初始均为EMPTY
        for (Dot[] dots : map) {
            for (Dot dot : dots) {
                dot.setStatus(Status.EMPTY);
            }
        }
        // cat状态为CAT，并设置起始点
        cat = new Dot(4, 5, Status.CAT);
        // 初始化路障
        for (int i = 0; i < NUM_BARRIERS; ) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);
            Dot dot = getDot(x, y);
            if (dot.getStatus() == Status.EMPTY) {
                dot.setStatus(Status.BARRIER);
                i++;
                Log.i("Random Barriers", String.format("x = %d, y = %d, i = %d", x, y ,i));
            }
        }
    }

    private void redraw() {
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
