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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import javax.imageio.ImageIO;

public class SnakeGame extends JPanel implements ActionListener, MouseMotionListener {

    private Client client = new Client();

    private final int WIDTH = 1000; // Kích thước khung nhìn (viewport)
    private final int HEIGHT = 800;
    private final int MAP_SIZE = 3000; // Kích thước bản đồ lớn hơn
    private final int DOT_SIZE = 35;
    private final ArrayList<Point> snake = new ArrayList<>();
    private Point food;
    private Point mousePosition = new Point(WIDTH / 2, HEIGHT / 2); // Vị trí chuột
    private boolean running = false;
    private Timer timer;
    private String id = null;
    private String playerName = "";
    Map<String, ArrayList<Point>> snakes = null;

    private final ArrayList<Food> foods = new ArrayList<>();
    private int score = 0; // Thêm trường điểm số
    private int viewportX = 0; // Tọa độ góc trên bên trái của viewport
    private int viewportY = 0;
    private Image backgroundImage; // Ảnh nền
    Random rand = new Random();
    private Color snakeColor = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()); // Tạo màu cố định cho rắn khi bắt đầu game

    Gson gson = new Gson();

    public SnakeGame(String playerName) {
        this.playerName = playerName; // Gán tên người chơi
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseMotionListener(this);

        // Tải ảnh từ đường dẫn
        try {
            backgroundImage = ImageIO.read(new File("D:\\Test\\SnakeGame\\Client\\GameVisual\\background.jpg"));
            if (backgroundImage == null) {
                System.out.println("Hình ảnh không được đọc.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không thể đọc tệp hình ảnh: " + e.getMessage());
        }

        startGame();
    }

    public void startGame() {
        snake.clear();
        snake.add(new Point(MAP_SIZE / 2, MAP_SIZE / 2)); // Vị trí khởi đầu của rắn
        viewportX = MAP_SIZE / 2 - WIDTH / 2;
        viewportY = MAP_SIZE / 2 - HEIGHT / 2;

        spawnFood(100);
        running = true;
        score = 0; // Đặt lại điểm số khi bắt đầu
        timer = new Timer(1000 / 165, this);
        timer.start();

        String tmp = client.receiveResponse();
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> parsedMap = gson.fromJson(tmp, mapType);
        id = (String) parsedMap.get("id");
    }

    public class Food {
        public Point position;  // Vị trí của food
        public int baseSize;  // Kích thước Food cơ bản
        public int currentSize;  // Kích thước Food hiện tại để nhấp nháy
        public Color color;     // Màu sắc của food
        public Color glowColor; // Màu phát sáng (hiệu ứng)
        private boolean increasing = true; // Điều khiển đốm sáng nhấp nháy
        private int blinkSpeed = 1; // Tốc độ nhấp nháy

        public Food(Point position, int size, Color color, Color glowColor) {
            this.position = position;
            this.baseSize = size;
            this.currentSize = size;
            this.color = color;
            this.glowColor = glowColor;
        }

        public void updateBlink() {
            if (increasing) {
                currentSize += blinkSpeed; // Tăng kích thước theo tốc độ nhấp nháy
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

    public void spawnFood(int numberOfFoods) {
        Random rand = new Random();
        for (int i = 0; i < numberOfFoods; i++) {
            int x = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
            int y = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
            int size = rand.nextInt(10) + 15;
            Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0.8f);
            Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);  // Semi-transparent glow

            foods.add(new Food(new Point(x, y), size, color, glowColor));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);
        
            // Lặp qua bản đồ của game và vẽ ảnh nền liên tục cho đến khi phủ kín
            for (int x = 0; x < MAP_SIZE; x += imageWidth) {
                for (int y = 0; y < MAP_SIZE; y += imageHeight) {
                    g.drawImage(backgroundImage, x - viewportX, y - viewportY, this);
                }
            }
        }
        
        if (running) {
            updateViewport();
            // Vẽ đồ ăn
            for (Food food : foods) {
                food.updateBlink();
                g.setColor(food.glowColor);
                g.fillOval(food.position.x - viewportX - (food.currentSize / 2), food.position.y - viewportY - (food.currentSize / 2), food.currentSize * 2, food.currentSize * 2);
                g.setColor(food.color);
                g.fillOval(food.position.x - viewportX, food.position.y - viewportY, food.currentSize, food.currentSize);
            }
            drawSnake(g);
            drawScore(g); // Vẽ điểm số
        } else {
            showGameOver(g);
        }
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30); // Hiển thị điểm số
    }

    public void drawSnake(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
        int outlineThickness = 3;
        Point head = snake.get(0);
        double angle = Math.atan2(mousePosition.y - head.y + viewportY, mousePosition.x - head.x + viewportX);
    
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(p.x - viewportX - outlineThickness, p.y - viewportY - outlineThickness, DOT_SIZE + 2 * outlineThickness, DOT_SIZE + 2 * outlineThickness);
            g2d.setColor(snakeColor);
            g2d.fillOval(p.x - viewportX, p.y - viewportY, DOT_SIZE, DOT_SIZE);
        }
    
        int eyeSize = 10;
        int eyeOffset = 12;
        int eyeVerticalOffset = 4;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(head.x - viewportX - eyeOffset, head.y - viewportY - eyeVerticalOffset, eyeSize, eyeSize);
        g2d.fillOval(head.x - viewportX + eyeOffset - eyeSize, head.y - viewportY - eyeVerticalOffset, eyeSize, eyeSize);
    
        int pupilSize = 5;
        g2d.setColor(Color.BLACK);
        int pupilOffsetX = (int) (Math.cos(angle) * 3);
        int pupilOffsetY = (int) (Math.sin(angle) * 3);
        g2d.fillOval(head.x - viewportX - eyeOffset + pupilOffsetX, head.y - viewportY - eyeVerticalOffset + pupilOffsetY, pupilSize, pupilSize);
        g2d.fillOval(head.x - viewportX + eyeOffset - eyeSize + pupilOffsetX, head.y - viewportY - eyeVerticalOffset + pupilOffsetY, pupilSize, pupilSize);
    
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Times New Roman", Font.BOLD, 20));
        g2d.drawString(playerName, head.x - viewportX, head.y - 20 - viewportY);
    }

    private double speedMultiplier = 0.2;

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

        double deltaX = mousePosition.x + viewportX - head.x;
        double deltaY = mousePosition.y + viewportY - head.y;
        double angle = Math.atan2(deltaY, deltaX);

        head.translate((int) (DOT_SIZE * Math.cos(angle) * speedMultiplier) / 2, 
                       (int) (DOT_SIZE * Math.sin(angle) * speedMultiplier) / 2);

        client.sendSnakePosition(snake);
        Type mapType = new TypeToken<Map<String, ArrayList<Point>>>() {}.getType();
        snakes = gson.fromJson(client.receiveResponse(), mapType);

        snake.add(0, head);

        boolean ateFood = false;

        for (int i = 0; i < foods.size(); i++) {
            Food food = foods.get(i);
            if (head.distance(food.position) < food.currentSize) {
                ateFood = true;
                foods.remove(i);
                spawnFood(1);
                score++; // Tăng điểm khi ăn food
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
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    public void updateViewport() {
        Point head = snake.get(0);
        double smoothFactor = 0.05;
        viewportX += (head.x - WIDTH / 2 - viewportX) * smoothFactor;
        viewportY += (head.y - HEIGHT / 2 - viewportY) * smoothFactor;
        viewportX = Math.max(0, Math.min(viewportX, MAP_SIZE - WIDTH));
        viewportY = Math.max(0, Math.min(viewportY, MAP_SIZE - HEIGHT));
    }

    public void showGameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Game Over", WIDTH / 2 - 80, HEIGHT / 2);
        g.drawString("Score: " + score, WIDTH / 2 - 50, HEIGHT / 2 + 40); // Hiển thị điểm số khi game over
        displayLeaderboard(g); // Hiển thị bảng điểm
    }

    public void displayLeaderboard(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Leaderboard", WIDTH / 2 - 50, HEIGHT / 2 + 80);
        
        // Tạm thời giả định có 5 người chơi với điểm số ngẫu nhiên
        for (int i = 0; i < 5; i++) {
            g.drawString("Player " + (i + 1) + ": " + (rand.nextInt(100) + 1), WIDTH / 2 - 50, HEIGHT / 2 + 120 + i * 30);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Không cần thiết xử lý sự kiện kéo chuột
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(18, 18, 18); // Màu nền tối
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
                    SnakeGame game = new SnakeGame(playerName);
                    gameFrame.add(game);
                    gameFrame.setResizable(false);
                    gameFrame.pack();
                    gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    gameFrame.setLocationRelativeTo(null);
                    gameFrame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a nickname.");
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
