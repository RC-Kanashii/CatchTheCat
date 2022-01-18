package com.rc.catchthecat.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rc.catchthecat.elements.Dir;
import com.rc.catchthecat.elements.Dot;
import com.rc.catchthecat.elements.Status;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import static com.rc.catchthecat.Playground.COL;
import static com.rc.catchthecat.Playground.ROW;
import static com.rc.catchthecat.Playground.cat;
import static com.rc.catchthecat.Playground.getDot;
import static com.rc.catchthecat.Playground.map;

public class GameLogic {
    private Context context;

    // Normal Mode
    private int[][] mapInfo;  // 用数值表示位置状态
    private Queue<Dot> dotQueue;  // 配合多源BFS使用，初始为地图边界的所有Dot

    public GameLogic(Context context) {
        this.context = context;
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

    private void catMoveTo(Dot dot) {
        dot.setStatus(Status.CAT);
        getDot(cat.getX(), cat.getY()).setStatus(Status.EMPTY);
        cat.setXY(dot.getX(), dot.getY());
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
            Log.i("catNeighbor", dir+": "+dot.getX()+", "+dot.getY());
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
            Log.i("catNeighbor", dir+": "+tmp.getX()+", "+tmp.getY());
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
        Log.i("map", "123");
    }

    private boolean checkXY(int x, int y) {
        if (x < 0 || x >= COL || y < 0 || y >= ROW) return false;
        return true;
    }

    private void gameWin() {
        Toast.makeText(context, "Win", Toast.LENGTH_SHORT).show();
    }

    private void gameLose() {
        Toast.makeText(context, "Lose", Toast.LENGTH_SHORT).show();
    }
}
