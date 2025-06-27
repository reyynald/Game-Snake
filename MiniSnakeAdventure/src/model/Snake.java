package model;

import java.util.LinkedList;

public class Snake {
    private LinkedList<Point> body;
    private Direction direction;

    public Snake(Point start, Direction direction) {
        body = new LinkedList<>();
        body.add(start);
        this.direction = direction;
    }

    public LinkedList<Point> getBody() {
        return body;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void move(Point newHead) {
        body.addFirst(newHead);
        body.removeLast();
    }

    public void grow(Point newHead) {
        body.addFirst(newHead);
    }

    public static class Point {
        public int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
