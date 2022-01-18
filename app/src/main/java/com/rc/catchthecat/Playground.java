package com.rc.catchthecat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.rc.catchthecat.elements.Dir;
import com.rc.catchthecat.elements.Dot;
import com.rc.catchthecat.elements.Status;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Playground extends SurfaceView implements View.OnTouchListener {


    /* 游戏基本信息 */

    // 行数
    private final int ROW = 10;
    // 列数
    private final int COL = 10;
    // Dot直径
    private int DOT_D = 80;
    // 偏移量
    private int OFFSET;
    // 地图与屏幕顶端的距离
    private int MARGIN_TOP;
    // 地图与屏幕左右两端的距离
    private int MARGIN_LR;
    // 默认添加的路障数量
    private final int NUM_BARRIERS = 12;
    // 地图
    private Dot[][] map;
    // 猫
    private Dot cat;
    // 随机数
    private Random random;
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

    private int[] images = {R.drawable.cat1, R.drawable.cat2, R.drawable.cat3,
            R.drawable.cat4, R.drawable.cat5, R.drawable.cat6, R.drawable.cat7,
            R.drawable.cat8, R.drawable.cat9, R.drawable.cat10,
            R.drawable.cat11, R.drawable.cat12, R.drawable.cat13,
            R.drawable.cat14, R.drawable.cat15, R.drawable.cat16};


    /* Normal难度 */

    private int[][] mapInfo;  // 用数值表示位置状态
    private Queue<Dot> dotQueue;  // 配合多源BFS使用，初始为地图边界的所有Dot


    /* 构造函数及相关初始化函数 */

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
        // 清空步数
        steps = 0;

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


    /* 绘制界面相关 */

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
            case "easy":
                diff_zh = "简单";
                break;
            case "normal":
                diff_zh = "普通";
                break;
            case "hard":
                diff_zh = "困难";
                break;
        }
        paint.setColor(Color.BLACK);
        paint.setTextSize(48);
        canvas.drawText("难度：" + diff_zh + "      步数：" + steps, 20, SCREEN_HEIGHT - 20, paint);

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


    /* 定时任务相关 */

    // 开启定时任务
    private void startTimer() {
        timer = new Timer();
        timerttask = new TimerTask() {
            public void run() {
                gifImage();
            }
        };
        timer.schedule(timerttask, 50, 92);
    }

    // 停止定时任务
    private void stopTimer() {
        timer.cancel();
        timer.purge();
    }


    /* 事件监控 */

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
                // initGame();
            } else {
                // 点击后设置为路障
                Dot dot = getDot(x, y);
                if (dot.getStatus() == Status.EMPTY) {
                    getDot(x, y).setStatus(Status.BARRIER);
                    switch (difficulty) {
                        case "easy":
                            moveEasy();
                            break;
                        case "normal":
                            moveNormal();
                            break;
                        case "hard":
                            moveNormal();
                            break;
                    }
                    steps++;
                }
            }
            redraw();
        }
        return true;
    }

    /* Utilities */

    private Dot getDot(int i, int j) {
        // 注意：游戏中行列坐标和此处的i, j是反着的
        return map[j][i];
    }

    private boolean isAtEdge(Dot dot) {
        if (dot.getX() * dot.getY() == 0 ||
                dot.getX() + 1 == COL ||
                dot.getY() + 1 == ROW) {
            return true;
        }
        return false;
    }

    private Dot getNeighbor(Dot dot, Dir dir) {
        Dot res = null;
        int x, y;
        switch (dir) {
            case L:
                x = dot.getX() - 1;
                y = dot.getY();
                if (checkXY(x, y)) res = getDot(x, y);
                break;
            case LU:
                if (dot.getY() % 2 == 0) {
                    x = dot.getX() - 1;
                    y = dot.getY() - 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                } else {
                    x = dot.getX();
                    y = dot.getY() - 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                }
                break;
            case RU:
                if (dot.getY() % 2 == 0) {
                    x = dot.getX();
                    y = dot.getY() - 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                } else {
                    x = dot.getX() + 1;
                    y = dot.getY() - 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                }
                break;
            case R:
                x = dot.getX() + 1;
                y = dot.getY();
                if (checkXY(x, y)) res = getDot(x, y);
                break;
            case RD:
                if (dot.getY() % 2 == 0) {
                    x = dot.getX();
                    y = dot.getY() + 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                } else {
                    x = dot.getX() + 1;
                    y = dot.getY() + 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                }
                break;
            case LD:
                if (dot.getY() % 2 == 0) {
                    x = dot.getX() - 1;
                    y = dot.getY() + 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                } else {
                    x = dot.getX();
                    y = dot.getY() + 1;
                    if (checkXY(x, y)) res = getDot(x, y);
                }
                break;
        }
        return res;
    }

    private boolean checkXY(int x, int y) {
        if (x < 0 || x >= COL || y < 0 || y >= ROW) return false;
        return true;
    }

    private void catMoveTo(Dot dot) {
        dot.setStatus(Status.CAT);
        getDot(cat.getX(), cat.getY()).setStatus(Status.EMPTY);
        cat.setXY(dot.getX(), dot.getY());
    }


    /* 游戏输赢 */

    private void gameWin() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("挑战成功");
        dialog.setMessage("你用" + (steps + 1) + "步捕捉到了小猫");
        dialog.setCancelable(false);
        dialog.setNegativeButton("重玩", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                initGame();
            }
        });
        dialog.show();
    }

    private void gameLose() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("挑战失败");
        dialog.setMessage("你让小猫逃走了");
        dialog.setCancelable(false);
        dialog.setNegativeButton("重玩", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                initGame();
            }
        });
        dialog.show();
    }


    /* Easy模式 */

    private int getDistance(Dot dot, Dir dir) {
        // 到达边界的距离（正数），或到达路障的距离（负数）
        int dis = 0;
        if (isAtEdge(dot)) {
            return 1;
        }
        // ori:初始点，next:下一个点
        Dot ori = dot, next;
        while (true) {
            next = getNeighbor(ori, dir);
            if (next.getStatus() == Status.BARRIER) {
                dis *= -1;
                break;
            } else if (isAtEdge(next)) {
                // 边界本身也是可到达的
                dis++;
                break;
            }
            dis++;
            ori = next;
        }
        return dis;
    }

    /**
     * 简单难度的移动算法，通过判断到达六个方向的路障、地图边界的距离而移动
     */
    public void moveEasy() {
        if (isAtEdge(cat)) {
            gameLose();
            return;
        }
        Dir[] dirs = Dir.values();  // 枚举所有的方向
        List<Dot> available = new ArrayList<>();
        List<Dot> positive = new ArrayList<>();
        HashMap<Dot, Dir> al = new HashMap<>();
        for (Dir dir : dirs) {
            Dot dot = getNeighbor(cat, dir);
            Log.i("catNeighbor", dir + ": " + dot.getX() + ", " + dot.getY());
            if (dot.getStatus() == Status.EMPTY) {
                available.add(dot);
                al.put(dot, dir);
                if (getDistance(dot, dir) > 0) {
                    positive.add(dot);
                }
            }
        }
        int len = available.size();
        if (len == 0) {
            gameWin();
        } else {
            // int index = random.nextInt(len);
            // catMoveTo(available.get(index));
            Dot best = null;
            // 存在可以直接到达屏幕边缘的走向
            if (positive.size() != 0) {
                int min = Integer.MAX_VALUE;
                for (int i = 0; i < positive.size(); i++) {
                    Dot tmp = positive.get(i);
                    int dis = getDistance(tmp, al.get(tmp));
                    if (dis < min) {
                        min = dis;
                        best = tmp;
                    }
                }
            } else { // 所有方向都存在路障
                int max = 0;
                for (int i = 0; i < available.size(); i++) {
                    Dot tmp = available.get(i);
                    int k = getDistance(tmp, al.get(tmp));
                    if (k < max) {
                        max = k;
                        best = tmp;
                    }
                }
            }
            catMoveTo(best);
        }
    }


    /* Normal模式 */

    public void moveNormal() {
        if (isAtEdge(cat)) {
            gameLose();
            return;
        }

        initMapInfo();
        initDotQueue();

        // 从边界开始进行多源BFS遍历，求出地图上所有点到边界的最短步长
        Dot dot;
        while (!dotQueue.isEmpty()) {
            dot = dotQueue.poll();
            for (Dir dir : Dir.values()) {
                Dot tmp = getNeighbor(dot, dir);
                if (tmp == null) continue;
                int x = dot.getX(), y = dot.getY();
                int nx = tmp.getX(), ny = tmp.getY();
                if (mapInfo[ny][nx] != 0) continue;  // 注意：mapInfo坐标和Dot坐标是相反的
                dotQueue.add(tmp);
                mapInfo[ny][nx] = mapInfo[y][x] + 1;  // 注意：mapInfo坐标和Dot坐标是相反的
            }
        }

        // 移动猫
        Dot best = null;
        int barrierNum = 0;
        int step = Integer.MAX_VALUE;
        for (Dir dir : Dir.values()) {
            Dot tmp = getNeighbor(cat, dir);
            Log.i("catNeighbor", dir + ": " + tmp.getX() + ", " + tmp.getY());
            if (mapInfo[tmp.getY()][tmp.getX()] == Integer.MAX_VALUE) {
                barrierNum++;
            }
            if (mapInfo[tmp.getY()][tmp.getX()] < step) {  // 注意：mapInfo坐标和Dot坐标是相反的
                step = mapInfo[tmp.getY()][tmp.getX()];
                best = tmp;
            }
        }
        if (best != null) catMoveTo(best);
        if (barrierNum >= 6) gameWin();

        for (int[] mapInfoRow : mapInfo) {
            System.out.println(Arrays.toString(mapInfoRow));
            Log.i("mapInfo", Arrays.toString(mapInfoRow));
        }
    }

    private void initMapInfo() {
        mapInfo = new int[ROW][COL];
        for (int i = 0; i < ROW; i++) mapInfo[i][0] = 1;  // 第一列
        for (int i = 0; i < ROW; i++) mapInfo[i][COL - 1] = 1;  // 最后一列
        for (int j = 1; j < COL - 1; j++) mapInfo[0][j] = 1;  // 第一排（去除首尾）
        for (int j = 1; j < COL - 1; j++) mapInfo[ROW - 1][j] = 1;  // 最后一排（去除首尾）
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                mapInfo[i][j] = map[i][j].getStatus() == Status.BARRIER ? Integer.MAX_VALUE : Math.max(0, mapInfo[i][j]);
            }
            Log.i("mapInfoRow", Arrays.toString(mapInfo[i]));
        }
    }

    private void initDotQueue() {
        dotQueue = new ArrayDeque<>();
        for (int i = 0; i < ROW; i++)
            if (map[i][0].getStatus() != Status.BARRIER) dotQueue.add(map[i][0]);  // 第一列
        for (int i = 0; i < ROW; i++)
            if (map[i][COL - 1].getStatus() != Status.BARRIER)
                dotQueue.add(map[i][COL - 1]);  // 最后一列
        for (int j = 1; j < COL - 1; j++)
            if (map[0][j].getStatus() != Status.BARRIER) dotQueue.add(map[0][j]);  // 第一排（去除首尾）
        for (int j = 1; j < COL - 1; j++)
            if (map[ROW - 1][j].getStatus() != Status.BARRIER)
                dotQueue.add(map[ROW - 1][j]);  // 最后一排（去除首尾）
    }
}
