package com.rc.catchthecat.services;

import android.content.Context;
import android.widget.Toast;

import com.rc.catchthecat.elements.Dir;
import com.rc.catchthecat.elements.Dot;
import com.rc.catchthecat.elements.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.rc.catchthecat.Playground.COL;
import static com.rc.catchthecat.Playground.ROW;
import static com.rc.catchthecat.Playground.cat;
import static com.rc.catchthecat.Playground.getDot;

public class GameLogic {
    private Context context;

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
        switch (dir) {
            case L:
                res = getDot(dot.getX() - 1, dot.getY());
                break;
            case LU:
                if (dot.getY() % 2 == 0) {
                    res = getDot(dot.getX() - 1, dot.getY() - 1);
                } else {
                    res = getDot(dot.getX(), dot.getY() - 1);
                }
                break;
            case RU:
                if (dot.getY() % 2 == 0) {
                    res = getDot(dot.getX(), dot.getY() - 1);
                } else {
                    res = getDot(dot.getX() + 1, dot.getY() - 1);
                }
                break;
            case R:
                res = getDot(dot.getX() + 1, dot.getY());
                break;
            case RD:
                if (dot.getY() % 2 == 0) {
                    res = getDot(dot.getX(), dot.getY() + 1);
                } else {
                    res = getDot(dot.getX() + 1, dot.getY() + 1);
                }
                break;
            case LD:
                if (dot.getY() % 2 == 0) {
                    res = getDot(dot.getX() - 1, dot.getY() + 1);
                } else {
                    res = getDot(dot.getX(), dot.getY() + 1);
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

    public void move() {
        if (isAtEdge(cat)) {
            gameLose();
            return;
        }
        Dir[] dirs = Dir.values();
        List<Dot> available = new ArrayList<>();
        List<Dot> positive = new ArrayList<>();
        HashMap<Dot, Dir> al = new HashMap<>();
        for (Dir dir : dirs) {
            Dot dot = getNeighbor(cat, dir);
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

    private void gameWin() {
        Toast.makeText(context, "Win", Toast.LENGTH_SHORT).show();
    }

    private void gameLose() {
        Toast.makeText(context, "Lose", Toast.LENGTH_SHORT).show();
    }
}
