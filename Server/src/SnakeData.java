package Server.src;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Color;

public class SnakeData {
    private ArrayList<Point> snakePoint;
    private double angle = 0;
    private String userName;
    private String color;
    private int score;
    
    public SnakeData(ArrayList<Point> snakePoint, String userName, String color, int score) {
        this.snakePoint = snakePoint;
        this.userName = userName;
        this.color = color;
        this.angle = 0;
        this.score = score;
    }

    public ArrayList<Point> getSnakePoint() {
        return snakePoint;
    }

    public void setSnakePoint(ArrayList<Point> snakePoint) {
        this.snakePoint = snakePoint;
    }

    public void updateSnakePoint(){
        Point head = new Point(snakePoint.get(0));

        head.translate((int) (Match.DOT_SIZE * Math.cos(this.angle) * Match.speedMultiplier) / 2,
           (int) (Match.DOT_SIZE * Math.sin(angle) * Match.speedMultiplier) / 2);
        head.x = Math.max(0, Math.min(head.x, Match.MAP_SIZE - Match.DOT_SIZE));
        head.y = Math.max(0, Math.min(head.y, Match.MAP_SIZE - Match.DOT_SIZE));

        snakePoint.add(0, head);
        snakePoint.remove(snakePoint.size() - 1);
    }

    public void addSnakePoint(){
        Point head = new Point(snakePoint.get(0));

        head.translate((int) (Match.DOT_SIZE * Math.cos(this.angle) * Match.speedMultiplier) / 2,
           (int) (Match.DOT_SIZE * Math.sin(angle) * Match.speedMultiplier) / 2);
        head.x = Math.max(0, Math.min(head.x, Match.MAP_SIZE - Match.DOT_SIZE));
        head.y = Math.max(0, Math.min(head.y, Match.MAP_SIZE - Match.DOT_SIZE));

        snakePoint.add(0, head);
    }


    public void setAngle(double angle) {
        this.angle = angle;
    }

    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
}
