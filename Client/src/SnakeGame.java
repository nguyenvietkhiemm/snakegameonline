package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class SnakeGame extends JPanel implements ActionListener, MouseMotionListener {

    private Client client = new Client();

    private final int WIDTH = 1000; // Kích thước khung nhìn (viewport)
    private final int HEIGHT = 800;
    private final int MAP_SIZE = 3000; // Kích thước bản đồ lớn hơn
    private final int DOT_SIZE = 20;
    private final ArrayList<Point> snake = new ArrayList<>();
    private Point food;
    private Point mousePosition = new Point(WIDTH / 2, HEIGHT / 2); // Vị trí chuột
    private boolean running = false;
    private Timer timer;
    private String id = null;

    private int viewportX = 0; // Tọa độ góc trên bên trái của viewport
    private int viewportY = 0;

    Gson gson = new Gson();

    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addMouseMotionListener(this);
        startGame();
    }

    public void startGame() {
        snake.clear();
        snake.add(new Point(MAP_SIZE / 2, MAP_SIZE / 2)); // Vị trí khởi đầu của rắn
        spawnFood();
        running = true;
        timer = new Timer(1000 / 60, this);
        timer.start();

        String tmp = client.receiveResponse();
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> parsedMap = gson.fromJson(tmp, mapType);
        System.out.println(parsedMap);
        // Kiểm tra kiểu của "id"

        id = (String) parsedMap.get("id");
        System.out.println(id);
    }

    public void spawnFood() {
        Random rand = new Random();
        int x = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
        int y = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
        food = new Point(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            // Di chuyển khung nhìn theo rắn
            updateViewport();

            // Vẽ lưới
            drawMap(g);

            // Vẽ đồ ăn
            g.setColor(Color.RED);
            g.fillOval(food.x - viewportX, food.y - viewportY, DOT_SIZE, DOT_SIZE);

            // Vẽ rắn
            g.setColor(Color.GREEN);
            for (Point p : snake) {
                g.fillOval(p.x - viewportX, p.y - viewportY, DOT_SIZE, DOT_SIZE);
            }
        } else {
            showGameOver(g);
        }
    }

    public void drawMap(Graphics g) {
        // Vẽ lưới ngang dọc với mỗi grid bằng 10 ô nhỏ
        g.setColor(Color.GRAY);
        int gridSize = 10 * DOT_SIZE;

        // Vẽ các đường dọc
        for (int i = 0; i <= MAP_SIZE; i += gridSize) {
            int xOnScreen = i - viewportX;
            if (xOnScreen >= 0 && xOnScreen <= WIDTH) {
                g.drawLine(xOnScreen, 0, xOnScreen, HEIGHT);
            }
        }

        // Vẽ các đường ngang
        for (int i = 0; i <= MAP_SIZE; i += gridSize) {
            int yOnScreen = i - viewportY;
            if (yOnScreen >= 0 && yOnScreen <= HEIGHT) {
                g.drawLine(0, yOnScreen, WIDTH, yOnScreen);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollision();
        }
        repaint();
    }

    public void move() {
        if (snake.isEmpty())
            return;

        Point head = new Point(snake.get(0));

        // Tính toán hướng từ đầu rắn đến vị trí chuột
        double deltaX = mousePosition.x + viewportX - head.x;
        double deltaY = mousePosition.y + viewportY - head.y;
        double angle = Math.atan2(deltaY, deltaX);

        // Di chuyển theo hướng của chuột
        head.translate((int) (DOT_SIZE * Math.cos(angle)), (int) (DOT_SIZE * Math.sin(angle)));

        client.sendSnakePosition(head);
        String tmp = client.receiveResponse();
        System.out.println("received:" + tmp);
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> snakes = gson.fromJson(tmp, mapType);
        System.out.println("snakes map: " + snakes);

        // Tính khoảng cách Euclidean giữa head và food
        double distance = head.distance(food);

        // Kiểm tra khoảng cách giữa head và food <= 0.8
        if (distance <= 0.9 * DOT_SIZE) {
            snake.add(0, head);
            spawnFood();
        } else {
            snake.add(0, head);
            snake.remove(snake.size() - 1);
        }
    }

    public void checkCollision() {
        Point head = snake.get(0);

        // Kiểm tra va chạm với bản thân
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }

        // Kiểm tra va chạm với biên
        if (head.x < 0 || head.x >= MAP_SIZE || head.y < 0 || head.y >= MAP_SIZE) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    public void updateViewport() {
        // Giữ rắn ở trung tâm khung nhìn (viewport)
        Point head = snake.get(0);
        viewportX = head.x - WIDTH / 2;
        viewportY = head.y - HEIGHT / 2;

        // Đảm bảo khung nhìn không ra khỏi bản đồ
        viewportX = Math.max(0, Math.min(viewportX, MAP_SIZE - WIDTH));
        viewportY = Math.max(0, Math.min(viewportY, MAP_SIZE - HEIGHT));
    }

    public void showGameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Game Over", WIDTH / 2 - 80, HEIGHT / 2);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Lấy vị trí chuột
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Không cần thiết xử lý sự kiện kéo chuột
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.setResizable(false);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
