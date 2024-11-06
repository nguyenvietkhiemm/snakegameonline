package Client.src;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Color;

public class SnakeData {
    private ArrayList<Point> snakePoint;
    private double angle = 0;
    private String userName;
    private String color;
    
    public SnakeData(ArrayList<Point> snakePoint, String userName, String color) {
        this.snakePoint = snakePoint;
        this.userName = userName;
        this.color = color;
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
}
