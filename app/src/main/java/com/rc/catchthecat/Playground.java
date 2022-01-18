package com.rc.catchthecat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.rc.catchthecat.elements.Dot;
import com.rc.catchthecat.elements.Status;
import com.rc.catchthecat.services.GameLogic;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Playground extends SurfaceView implements View.OnTouchListener {
    // 行数
    public static final int ROW = 10;
    // 列数
    public static final int COL = 10;
    // Dot直径
    public static int DOT_D = 80;
    // 偏移量
    private int OFFSET;
    // 地图与屏幕顶端的距离
    private int MARGIN_TOP;
    // 地图与屏幕左右两端的距离
    private int MARGIN_LR;
    // 默认添加的路障数量
    private final int NUM_BARRIERS = 12;
    // 地图
    public static Dot[][] map;
    // 猫
    public static Dot cat;
    // 随机数
    private Random random;
    // GameLogic对象
    GameLogic gameLogic;
    // 游戏难度
    String difficulty;

    // 屏幕宽度
    private int SCREEN_WIDTH;
    // 屏幕高度
    private int SCREEN_HEIGHT;
    // 做成神经猫动态图效果的单张图片
    private Drawable cat_drawable;
    // 背景图
    private Drawable background;
    // 神经猫动态图的索引
    private int index = 0;
    private Timer timer;

    private TimerTask timerttask;

    private Context context;
    //行走的步数
    private int steps;

    private boolean canMove = true;

    private int[] images = {R.drawable.cat1, R.drawable.cat2, R.drawable.cat3,
            R.drawable.cat4, R.drawable.cat5, R.drawable.cat6, R.drawable.cat7,
            R.drawable.cat8, R.drawable.cat9, R.drawable.cat10,
            R.drawable.cat11, R.drawable.cat12, R.drawable.cat13,
            R.drawable.cat14, R.drawable.cat15, R.drawable.cat16};

    public Playground(Context context) {
        super(context);
        this.context = context;
        initSurfaceView();
    }

    public Playground(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initSurfaceView();
    }

    public Playground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initSurfaceView();
    }

    public Playground(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initSurfaceView();
    }

    private void initSurfaceView() {
        // 传递回调类
        getHolder().addCallback(callback);
        random = new Random();
        gameLogic = new GameLogic(getContext());
        setOnTouchListener(this);

        if (Build.VERSION.SDK_INT < 21) {
            cat_drawable = getResources().getDrawable(images[index]);
            background = getResources().getDrawable(R.drawable.bg);
        } else {
            cat_drawable = getResources().getDrawable(images[index], null);
            background = getResources().getDrawable(R.drawable.bg, null);
        }

        initMap();
        initGame();
        initDifficulty();
    }

    private void initDifficulty() {
        SharedPreferences sp = context.getSharedPreferences("difficulty", Context.MODE_PRIVATE);
        difficulty = sp.getString("difficulty", "easy");  // 默认easy
    }

    public static Dot getDot(int i, int j) {
        // 注意：游戏中行列坐标和此处的i, j是反着的
        return map[j][i];
    }

    private void initMap() {
        steps = 0;

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
        cat = new Dot(5, 5, Status.CAT);
        getDot(5, 5).setStatus(Status.CAT);
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
                        new RectF(dot.getX() * DOT_D + OFFSET + MARGIN_LR,
                                dot.getY() * DOT_D + MARGIN_TOP,
                                (dot.getX() + 1) * DOT_D + OFFSET + MARGIN_LR,
                                (dot.getY() + 1) * DOT_D + MARGIN_TOP),
                        paint);
            }
        }

        String diff_zh = "";
        switch (difficulty) {
            case "easy": diff_zh = "简单"; break;
            case "normal": diff_zh = "普通"; break;
            case "hard": diff_zh = "困难"; break;
        }
        paint.setColor(Color.BLACK);
        paint.setTextSize(68);
        canvas.drawText("难度：" + diff_zh, 20, SCREEN_HEIGHT - 20, paint);

        int left;
        int top;
        if (cat.getY() % 2 == 0) {
            left = cat.getX() * DOT_D;
            top = cat.getY() * DOT_D;
        } else {
            left = (DOT_D / 2) + cat.getX() * DOT_D;
            top = cat.getY() * DOT_D;
        }
        // 此处神经猫图片的位置是根据效果图来调整的
        cat_drawable.setBounds(left - DOT_D / 6 + MARGIN_LR, top - DOT_D / 2
                + MARGIN_TOP, left + DOT_D + MARGIN_LR, top + DOT_D + MARGIN_TOP);
        cat_drawable.draw(canvas);
        background.setBounds(0, 0, SCREEN_WIDTH, MARGIN_TOP);
        background.draw(canvas);

        // 将画布内容进行提交
        getHolder().unlockCanvasAndPost(canvas);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
            // 第一次显示时，把内容显示在屏幕上
            redraw();
            startTimer();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
            // 动态设置Dot直径
            DOT_D = width / (COL + 1);
            SCREEN_WIDTH = width;
            SCREEN_HEIGHT = height;
            MARGIN_TOP = height - DOT_D * (ROW + 2);
            MARGIN_LR = DOT_D / 3;

            // 重新绘制界面
            redraw();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
            stopTimer();
        }
    };

    // 开启定时任务
    private void startTimer() {
        timer = new Timer();
        timerttask = new TimerTask() {
            public void run() {
                gifImage();
            }
        };
        timer.schedule(timerttask, 50, 65);
    }

    // 停止定时任务
    public void stopTimer() {
        timer.cancel();
        timer.purge();
    }

    // 动态图
    private void gifImage() {
        index++;
        if (index > images.length - 1) {
            index = 0;
        }
        if (Build.VERSION.SDK_INT < 21) {
            cat_drawable = getResources().getDrawable(images[index]);
        } else {
            cat_drawable = getResources().getDrawable(images[index], null);
        }
        redraw();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // 对玩家按下后抬起做识别
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // Toast.makeText(getContext(), motionEvent.getX() + ":" + motionEvent.getY(), Toast.LENGTH_SHORT).show();
            int x, y;
            y = (int) ((motionEvent.getY() - MARGIN_TOP) / DOT_D);
            // 设置奇偶行的偏移量
            OFFSET = y % 2 == 0 ? 0 : DOT_D / 2;
            x = (int) ((motionEvent.getX() - OFFSET - MARGIN_LR) / DOT_D);
            if (x >= COL || y >= ROW || y < 0) {
                initGame();
            } else {
                // 点击后设置为路障
                Dot dot = getDot(x, y);
                if (dot.getStatus() == Status.EMPTY) {
                    getDot(x, y).setStatus(Status.BARRIER);
                    switch (difficulty) {
                        case "easy": gameLogic.moveEasy(); break;
                        case "normal": gameLogic.moveNormal(); break;
                        case "hard": gameLogic.moveNormal(); break;
                    }
                }
            }
            redraw();
        }
        return true;
    }
}
