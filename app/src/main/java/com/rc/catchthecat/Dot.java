package com.rc.catchthecat;

public class Dot {
    private int x;
    private int y;
    private Status status;

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
        this.status = Status.EMPTY;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
