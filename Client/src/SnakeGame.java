package Client.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import javax.imageio.ImageIO;

public class SnakeGame extends JPanel implements ActionListener, MouseMotionListener {
    private Client client = new Client();

    private final int WIDTH = 1000;
    private final int HEIGHT = 800;
    private final int MAP_SIZE = 1000;
    private final int DOT_SIZE = 35;

    private Point mousePosition = new Point(WIDTH / 2, HEIGHT / 2);
    private final ArrayList<Point> snake = new ArrayList<>();
    Map<String, SnakeData> snakes = new HashMap<>();
    private boolean running = false;
    private Timer timer;
    private String id = null;
    private String playerName = "";
    private final ArrayList<Food> foods = new ArrayList<>();
    private int score = 0;
    private int viewportX = 0;
    private int viewportY = 0;
    private Image backgroundImage;
    Random rand = new Random();
    private Color snakeColor;
    private double speedMultiplier = 0.2;
    private float red = rand.nextFloat();
    private float green = rand.nextFloat();
    private float blue = rand.nextFloat();
    Gson gson = new Gson();
    private JFrame gameFrame; // Thêm biến gameFrame để lưu trữ JFrame

    public SnakeGame(JFrame gameFrame) { // Nhận JFrame từ constructor
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseMotionListener(this);

        try {
            backgroundImage = ImageIO.read(new File("GameVisual/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        startGame();
    }

    public void startGame() {
        snake.clear();
        snake.add(new Point(MAP_SIZE / 2, MAP_SIZE / 2));
        viewportX = MAP_SIZE / 2 - WIDTH / 2;
        viewportY = MAP_SIZE / 2 - HEIGHT / 2;

        snakeColor = new Color(red, green, blue);
        running = true;
        score = 0;
        timer = new Timer(1, this);
        SnakeData snakeData = new SnakeData(snake, playerName, red, green, blue, true);
        client.sendSnakeData(snakeData);
        timer.start();

        String tmp = client.receiveResponse();
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> parsedMap = gson.fromJson(tmp, mapType);
        id = parsedMap.get("id");
        System.out.println(id);
    }

    public void spawnFood(int numberOfFoods) {
        for (int i = 0; i < numberOfFoods; i++) {
            int x = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
            int y = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
            int size = rand.nextInt(10) + 15;
            Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0.8f);
            Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
            foods.add(new Food(new Point(x, y), size, color, glowColor));
        }
    }

    @Override
    protected void paintComponent(Graphics g) { 
        super.paintComponent(g);
        if (backgroundImage != null) {
            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);
            for (int x = 0; x < MAP_SIZE; x += imageWidth) {
                for (int y = 0; y < MAP_SIZE; y += imageHeight) {
                    g.drawImage(backgroundImage, x - viewportX, y - viewportY, this);
                }
            }
        }

        if (running) {
            updateViewport();
            for (Food food : foods) {
                food.updateBlink();
                g.setColor(food.glowColor);
                g.fillOval(food.position.x - viewportX - (food.currentSize / 2),
                        food.position.y - viewportY - (food.currentSize / 2), food.currentSize * 2,
                        food.currentSize * 2);
                g.setColor(food.color);
                g.fillOval(food.position.x - viewportX, food.position.y - viewportY, food.currentSize,
                        food.currentSize);
            }

            for (Map.Entry<String, SnakeData> snake : snakes.entrySet()) {
                SnakeData snakeData = snake.getValue();
                if(snakeData.isAlive()){
                if (snakeData == null || !snakes.containsKey(snake.getKey())) continue;
                Point head = snakeData.getSnakePoint().get(0);
                double angle = Math.atan2(mousePosition.y - head.y + viewportY, mousePosition.x - head.x + viewportX);

                g.setColor(new Color(snakeData.getRed(), snakeData.getGreen(), snakeData.getBlue()));
                for (Point p : snakeData.getSnakePoint()) {
                    g.fillOval(p.x - viewportX, p.y - viewportY, DOT_SIZE, DOT_SIZE);
                }

                int eyeSize = 10;
                int eyeOffset = 12;
                int eyeVerticalOffset = 4;

                g.setColor(Color.WHITE);
                g.fillOval(head.x - viewportX - eyeOffset, head.y - viewportY - eyeVerticalOffset, eyeSize, eyeSize);
                g.fillOval(head.x - viewportX + eyeOffset - eyeSize, head.y - viewportY - eyeVerticalOffset, eyeSize,
                        eyeSize);

                int pupilSize = 5;
                g.setColor(Color.BLACK);
                int pupilOffsetX = (int) (Math.cos(angle) * 3);
                int pupilOffsetY = (int) (Math.sin(angle) * 3);
                g.fillOval(head.x - viewportX - eyeOffset + pupilOffsetX,
                        head.y - viewportY - eyeVerticalOffset + pupilOffsetY, pupilSize, pupilSize);
                g.fillOval(head.x - viewportX + eyeOffset - eyeSize + pupilOffsetX,
                        head.y - viewportY - eyeVerticalOffset + pupilOffsetY, pupilSize, pupilSize);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Times New Roman", Font.BOLD, 20));
                g.drawString(snakeData.getUserName(), head.x - viewportX, head.y - 20 - viewportY);
            }
        }
            drawScore(g);
        }
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Điểm: " + score, 10, 30);
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
        if (snake.isEmpty()) return;

        Point head = new Point(snake.get(0));
        double deltaX = mousePosition.x + viewportX - head.x;
        double deltaY = mousePosition.y + viewportY - head.y;
        double angle = Math.atan2(deltaY, deltaX);

        head.translate((int) (DOT_SIZE * Math.cos(angle) * speedMultiplier) / 2,
                (int) (DOT_SIZE * Math.sin(angle) * speedMultiplier) / 2);

        client.sendSnakeLocation(snake);
        Type mapType = new TypeToken<Map<String, SnakeData>>() {
        }.getType();
        snakes = gson.fromJson(client.receiveResponse(), mapType);

        snake.add(0, head);
        boolean ateFood = false;

        for (int i = 0; i < foods.size(); i++) {
            Food food = foods.get(i);
            if (head.distance(food.position) < food.currentSize) {
                ateFood = true;
                foods.remove(i);
                spawnFood(1);
                score++;
                break;
            }
        }

        if (!ateFood) {
            snake.remove(snake.size() - 1);
        }
    }

    public void checkCollision() {
        Point head = snake.get(0);

        if (head.x < 0 || head.x >= MAP_SIZE || head.y < 0 || head.y >= MAP_SIZE) {
            handleSnakeDeath(id);
            return;
        }

        for (Map.Entry<String, SnakeData> entry : new HashMap<>(snakes).entrySet()) {
            String snakeId = entry.getKey();
            SnakeData otherSnake = entry.getValue();

            if (snakeId.equals(id)) continue;

            for (Point bodyPoint : otherSnake.getSnakePoint()) {
                if (head.distance(bodyPoint) < DOT_SIZE - 10) {
                    handleSnakeDeath(id);
                    return;
                }
            }
        }
    }

    private void handleSnakeDeath(String snakeId) {
        SnakeData deadSnake = snakes.get(snakeId);
        deadSnake.setAlive(false);

        if (snakeId.equals(id)) {
            endGame();
        }
        repaint();
    }

    private void endGame() {
        running = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! " + "\n" + "Điểm số của bạn là: " + score);
        gameFrame.dispose(); // Đóng cửa sổ trò chơi sau khi nhấn "OK"
    }

    public void updateViewport() {
        Point head = snake.get(0);
        double smoothFactor = 0.05;
        viewportX += (head.x - WIDTH / 2 - viewportX) * smoothFactor;
        viewportY += (head.y - HEIGHT / 2 - viewportY) * smoothFactor;
        viewportX = Math.max(0, Math.min(viewportX, MAP_SIZE - WIDTH));
        viewportY = Math.max(0, Math.min(viewportY, MAP_SIZE - HEIGHT));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(18, 18, 18);
                Color color2 = new Color(0, 0, 0);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Snake Game", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(new Color(0, 255, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 30));
        nameField.setBackground(new Color(48, 25, 52));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(150, 75, 0), 2));

        JButton playButton = new JButton("Play");
        playButton.setBackground(new Color(60, 180, 75));
        playButton.setForeground(Color.WHITE);
        playButton.setFont(new Font("Arial", Font.BOLD, 30));
        playButton.setFocusPainted(false);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText();
                if (!playerName.trim().isEmpty()) {
                    frame.dispose();
                    JFrame gameFrame = new JFrame("Snake Game");
                    SnakeGame game = new SnakeGame(gameFrame); // Truyền gameFrame vào SnakeGame
                    gameFrame.add(game);
                    gameFrame.setResizable(false);
                    gameFrame.pack();
                    gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    gameFrame.setLocationRelativeTo(null);
                    gameFrame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(frame, "Vui lòng nhập tên");
                }
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(nameField, BorderLayout.NORTH);
        inputPanel.add(playButton, BorderLayout.SOUTH);
        panel.add(inputPanel, BorderLayout.CENTER);

        frame.add(panel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
