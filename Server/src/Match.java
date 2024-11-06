package Server.src;

import java.util.*;
import java.awt.*;

public class Match {
    static Map<String, SnakeData> snakes = new HashMap<>();
    static ArrayList<Food> foods = new ArrayList<>();
    
    public static final int MAP_SIZE = 5000;
    public static final int DOT_SIZE = 35;
    public static final double speedMultiplier = 0.2;

    static Random rand = new Random();

    static{
        for (int i = 0; i < 100; i++) {
            addFood();
        }
    }

    public static Map<String, SnakeData> getSnakes() {
        return snakes;
    }

    public static void updateSnake(String id, SnakeData snakeData) {
        snakes.put(id, snakeData);
    }
    public static void updateSnakesPoint(){
        for (SnakeData snakeData : snakes.values()) {
            snakeData.updateSnakePoint();
        }
    }

    public static SnakeData getSnakeDataById(String id) {
        return snakes.get(id);
    }
    
    public static void removeSnake(String id) {
        snakes.remove(id);
    }


    public static ArrayList<Food> getFoods() {
        return foods;
    }
    public static void addFood() {
        int x = (int) (Math.random() * (MAP_SIZE / DOT_SIZE)) * DOT_SIZE;
        int y = (int) (Math.random() * (MAP_SIZE / DOT_SIZE)) * DOT_SIZE;
        int size = rand.nextInt(10) + 15;
        String color = String.format("#%02X%02X%02X", rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

        Food food = new Food(new Point(x, y), size, color);
        foods.add(food);
    }
    public static void removeFood(Point location) {
        foods.remove(location);
    }
    public static void checkSnakeEatFood() {
        for (SnakeData snake : snakes.values()) {
            Point head = new Point(snake.getSnakePoint().get(0));
            Iterator<Food> foodIterator = foods.iterator();
            
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                
                if (head.distance(food.getPosition()) < DOT_SIZE) {
                    for (int i = 0; i < food.getSize()/5; i++) {
                        snake.addSnakePoint();
                    }
                    foodIterator.remove();
                    
                    addFood();
                }
            }
        }
    }
    public static void checkSnakeCollision() {
        ArrayList<String> toRemove = new ArrayList<>();
    
        for (Map.Entry<String, SnakeData> entryA : snakes.entrySet()) {
            String idA = entryA.getKey();
            SnakeData snakeA = entryA.getValue();
            Point headA = new Point(snakeA.getSnakePoint().get(0));
    
            for (Map.Entry<String, SnakeData> entryB : snakes.entrySet()) {
                String idB = entryB.getKey();
                SnakeData snakeB = entryB.getValue();
    
                // Kiểm tra nếu head của snakeA đâm vào bất kỳ điểm nào của thân snakeB (bỏ qua head)
                if (!idA.equals(idB)) {
                    for (int i = 1; i < snakeB.getSnakePoint().size(); i++) {
                        Point bodyPart = snakeB.getSnakePoint().get(i);
                        if (headA.distance(bodyPart) < DOT_SIZE) {
                            toRemove.add(idA);
                            break;
                        }
                    }
                }
            }
        }
    
        for (String id : toRemove) {
            snakes.remove(id);
        }
    }
}
