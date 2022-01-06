package com.rc.catchthecat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Random;

public class Playground extends SurfaceView implements View.OnTouchListener {
    // 行数
    private final int ROW = 10;
    // 列数
    private final int COL = 10;
    // Dot直径
    private int DOT_D = 80;
    // 偏移量
    private int OFFSET;
    // 默认添加的路障数量
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
        setOnTouchListener(this);
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
        getDot(4, 5).setStatus(Status.CAT);
        // 初始化路障
        for (int i = 0; i < NUM_BARRIERS; ) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);
            Dot dot = getDot(x, y);
            if (dot.getStatus() == Status.EMPTY) {
                dot.setStatus(Status.BARRIER);
                i++;
                Log.i("Random Barriers", String.format("x = %d, y = %d, i = %d", x, y, i));
            }
        }
    }

    private void redraw() {
        // 获取Canvas对象
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.CYAN);

        // 绘制Dot
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROW; i++) {
            // 设置奇偶行的偏移量
            OFFSET = i % 2 == 0 ? 0 : DOT_D / 2;
            for (int j = 0; j < COL; j++) {
                Dot dot = getDot(j, i);
                // 设置画笔颜色
                switch (dot.getStatus()) {
                    case EMPTY:
                        paint.setColor(getResources().getColor(R.color.colorEmpty));
                        break;
                    case BARRIER:
                        paint.setColor(getResources().getColor(R.color.colorBarrier));
                        break;
                    case CAT:
                        paint.setColor(getResources().getColor(R.color.colorCat));
                        break;
                    default:
                        paint.setColor(Color.LTGRAY);
                        break;
                }
                canvas.drawOval(
                        new RectF(dot.getX() * DOT_D + OFFSET,
                                dot.getY() * DOT_D,
                                (dot.getX() + 1) * DOT_D + OFFSET,
                                (dot.getY() + 1) * DOT_D),
                        paint);
            }
        }

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
        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
            // 动态设置Dot直径
            DOT_D = width / (COL + 1);
            // 重新绘制界面
            redraw();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // 对玩家按下后抬起做识别
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Toast.makeText(getContext(), motionEvent.getX() + ":" + motionEvent.getY(), Toast.LENGTH_SHORT).show();
            int x, y;
            y = (int) (motionEvent.getY() / DOT_D);
            // 设置奇偶行的偏移量
            OFFSET = y % 2 == 0 ? 0 : DOT_D / 2;
            x = (int) ((motionEvent.getX() - OFFSET) / DOT_D);
            if (x >= COL || y >= ROW) {
                initGame();
            } else {
                // 点击后设置为路障
                Dot dot = getDot(x, y);
                if (dot.getStatus() == Status.EMPTY){
                    getDot(x, y).setStatus(Status.BARRIER);
                }
            }
            redraw();
        }
        return true;
    }
}
