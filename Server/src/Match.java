package Server;

import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map
import java.util.ArrayList;
import java.awt.*;

public class Match {
    static Map<String, ArrayList<Point>> snakes = new HashMap<>();

    public static Map<String, ArrayList<Point>> getSnakes() {
        return snakes;
    }

    public static void updateSnake(String id, ArrayList<Point> snake) {
        snakes.put(id, snake);
    }

    public static void removeSnake(String id) {
        snakes.remove(id);
    }
}
