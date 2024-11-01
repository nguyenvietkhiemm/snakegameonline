package Server.src;

import java.awt.*;
import java.util.*;

public class Food {
    public Point position;
    public int size;
    public Color color;
    public Color glowColor;

    public Food(Point position, int size, Color color, Color glowColor) {
        this.position = position;
        this.size = size;
        this.color = color;
        this.glowColor = glowColor;
    }
    public Point getPosition() { return position; }
    public int getSize() { return size; }
    public Color getColor() { return color; }
    public Color getGlowColor() { return glowColor; }
}