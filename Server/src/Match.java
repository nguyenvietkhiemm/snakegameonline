package Server;

import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map

public class Match {
    static Snake[] snakes = new Snake[5];

    public static Map<String, Object> getSnakes(){
        Map<String, Object> snakesMap = new HashMap<>();

        for (Snake snake : snakes)
        {
            snakesMap.put("id", snake.getId());
            snakesMap.put("location", snake.location());
        }

        return snakesMap;
    }

    public static void addSnake(Snake snake)
    {
        for (int i = 0; i < snakes.length; i++)
        {
            if (snakes[i] == null)
            {
                snakes[i] = snake;
                return;
            }
        }
    }
}
