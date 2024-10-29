package Server.src;

import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map
import java.util.ArrayList;
import java.awt.*;

public class Match {
    static Map<String, SnakeData> snakes = new HashMap<>();

    public static Map<String, SnakeData> getSnakes() {
        return snakes;
    }

    public static void updateSnake(String id, SnakeData snakeData) {
        snakes.put(id, snakeData);
    }

    public static SnakeData getSnakeDataById(String id) {
        return snakes.get(id);
    }
    
    public static void removeSnake(String id) {
        snakes.remove(id);
    }
}
