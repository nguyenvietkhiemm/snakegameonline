package Client.src;

import java.awt.*;

public class Food {
    public Point position;
    public int size;
    public String color;

    public Food(Point position, int size, String color) {
        this.position = position;
        this.size = size;
        this.color = color;
    }
    public Point getPosition() { return this.position; }
    public int getSize() { return this.size; }
    public Color getColor(){
        return Color.decode(this.color);
    }

    public void setPosition(Point position) {
        this.position = position;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setSize(int size) {
        this.size = size;
    }
}