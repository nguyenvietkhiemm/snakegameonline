package Server.src;

import java.awt.*;

public class Food {
    public Point position;
    public int size;

    public Food(Point position, int size) {
        this.position = position;
        this.size = size;
    }
    public Point getPosition() { 
        return this.position; 
    }
    
    public int getSize() { 
        return this.size; 
    }


    public void setPosition(Point position) {
        this.position = position;
    }

    public void setSize(int size) {
        this.size = size;
    }
}