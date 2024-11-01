package Server.src;

import java.util.*;
import java.awt.*;

public class Match {
    static Map<String, SnakeData> snakes = new HashMap<>();
    static ArrayList<Food> foods = new ArrayList<>();
    
    private final int WIDTH = 1000;
    private final int HEIGHT = 800;
    private final int MAP_SIZE = 1000;
    private final int DOT_SIZE = 35;

    Random rand = new Random();

    public Match(){
        // FOOD SPAWN
        for (int i = 0; i < 20; i++) {
            int x = (int) (Math.random() * (MAP_SIZE / DOT_SIZE)) * DOT_SIZE;
            int y = (int) (Math.random() * (MAP_SIZE / DOT_SIZE)) * DOT_SIZE;
            int size = rand.nextInt(10) + 15;
            Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0.8f);
            Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);

            Food food = new Food(new Point(x, y), size, color, glowColor);
            foods.add(food);
        }
    }

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

    public static ArrayList<Map<String, Object>> getFoods() {
        ArrayList<Map<String, Object>> foodsData = new ArrayList<Map<String, Object>>();
        for (Food food : foods) {
            Map<String, Object> foodData = new HashMap<>();
            foodData.put("position", food.getPosition());
            foodData.put("size", food.getSize());
            foodData.put("color", food.getColor());
            foodData.put("glowColor", food.getGlowColor());

            foodsData.add(foodData);
        }

        return foodsData;
    }
    public static void removeFood(Point location) {
        foods.remove(location);
    }
}
