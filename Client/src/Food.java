package Client.src;

import java.awt.*;

public class Food {
    public Point position;
    public int baseSize;
    public int currentSize;
    public Color color;
    public Color glowColor;
    private boolean increasing = true;
    private int blinkSpeed = 1;

    public Food(Point position, int size, Color color, Color glowColor) {
        this.position = position;
        this.baseSize = size;
        this.currentSize = size;
        this.color = color;
        this.glowColor = glowColor;
    }

    public void updateBlink() {
        if (increasing) {
            currentSize += blinkSpeed;
            if (currentSize >= baseSize + 5) {
                increasing = false;
            }
        } else {
            currentSize -= blinkSpeed;
            if (currentSize <= baseSize - 5) {
                increasing = true;
            }
        }
    }
}