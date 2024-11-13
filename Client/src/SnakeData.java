package Client.src;

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
        this.score = score;
    }

    public ArrayList<Point> getSnakePoint() {
        return snakePoint;
    }

    public void setSnakePoint(ArrayList<Point> snakePoint) {
        this.snakePoint = snakePoint;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }
    public double getAngle() {
        return this.angle;
    }
    public Color getColor(){
        return Color.decode(this.color);
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
