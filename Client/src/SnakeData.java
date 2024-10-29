package Client.src;

import java.util.ArrayList;
import java.awt.Point;

public class SnakeData {
    private ArrayList<Point> snakePoint;
    private String userName;
    private float red;
    private float green;
    private float blue;
    private boolean alive;
    
    public SnakeData(ArrayList<Point> snakePoint, String userName, float red, float green, float blue, boolean alive) {
        this.snakePoint = snakePoint;
        this.userName = userName;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alive = alive;
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

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    
   
}
