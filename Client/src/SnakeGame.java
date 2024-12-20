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
import java.util.List;


public class SnakeGame extends JPanel implements ActionListener, MouseMotionListener {
    private Client client = new Client();

    private final int WIDTH = 1000;
    private final int HEIGHT = 800;
    private final int MAP_SIZE = 3000;
    private final int DOT_SIZE = 35;

    private Point mousePosition = new Point(MAP_SIZE / 5 - WIDTH / 2, MAP_SIZE / 5 - HEIGHT / 2);
    private ArrayList<Point> snake = new ArrayList<>();
    private Point head = new Point();
    Map<String, SnakeData> snakes = new HashMap<>();
    Map<String, SnakeData> leaderboard = new HashMap<>(); //bảng xếp hạng
    private boolean running = false;
    private Timer timer;
    public static String id = null;
    private String playerName = "";

    private ArrayList<Food> foods = new ArrayList<>();
    private int score = 0;
    private int viewportX = 0;
    private int viewportY = 0;
    private Image backgroundImage;
    private Image appleImage;

    Random rand = new Random();
    private String snakeColor;
    private Gson gson = new Gson();
    private JFrame gameFrame;

    public SnakeGame(JFrame gameFrame, String playerName) {
        this.gameFrame = gameFrame;
        this.playerName = playerName;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseMotionListener(this);

        try {
            backgroundImage = ImageIO.read(new File("GameVisual/background.jpg"));
            appleImage = ImageIO.read(new File("GameVisual/apple.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        startGame();
    }

    public void startGame() {
        snake.clear();
        snake.add(new Point(rand.nextInt(MAP_SIZE), rand.nextInt(MAP_SIZE)));
        head = snake.get(0);

        snakeColor = String.format("#%02X%02X%02X", rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        running = true;
        score = 0;
        timer = new Timer(1000 / 165, this);

        SnakeData snakeData = new SnakeData(snake, playerName, snakeColor, score);
        client.sendSnakeData(snakeData);
        timer.start();

        String tmp = client.receiveResponse();
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> parsedMap = gson.fromJson(tmp, mapType);
        id = parsedMap.get("id");
        System.out.println(id);

        new Thread(this::receiveResponsesLoopSnakes).start();
        new Thread(this::sendSnakeMovingLoop).start();
    }

    private void sendSnakeMovingLoop(){
        while (running) {
            if (snake.isEmpty())
                return;

            int adjustedHeadX = head.x - viewportX;
            int adjustedHeadY = head.y - viewportY;
            client.sendSnakeDirection(new Point(adjustedHeadX, adjustedHeadY), mousePosition);
            
            try {
                Thread.sleep(1000 / 120);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void receiveResponsesLoopSnakes() {
        long startTime = System.currentTimeMillis();
        boolean idFound = false;
        while (running) {
            try {
                String response = client.receiveResponse();
                synchronized (this) {
                    Type mapType = new TypeToken<Map<String, SnakeData>>() {}.getType();
                    snakes = gson.fromJson(response, mapType);
                    // Kiểm tra nếu `id` tồn tại trong `snakes`
                    if (snakes.containsKey(id)) {
                        // idFound = true;
                        startTime = System.currentTimeMillis();
                        snake = snakes.get(id).getSnakePoint();
                        head = snake.get(0);
                        score = snakes.get(id).getScore();
                        System.out.println("Snake data updated: " + snake);
                    } else if ((System.currentTimeMillis() - startTime >= 2000)) {
                        System.out.println("Snake with id " + id + " not found for 2 seconds. Ending game.");
                        running = false;
                        endGame();
                        client.closeConnection();
                        break;
                    }
                }

                String responseFoods = client.receiveResponse();
                synchronized (this) {
                    Type foodType = new TypeToken<ArrayList<Food>>() {}.getType();
                    foods = gson.fromJson(responseFoods, foodType);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
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
                int halfSize = food.getSize() / 2;
                Point position = food.getPosition();
                int posX = position.x - halfSize - viewportX;
                int posY = position.y - halfSize - viewportY;
                
                // Vẽ ảnh quả táo nếu ảnh đã được tải thành công
                if (appleImage != null) {
                    g.drawImage(appleImage, posX, posY, food.getSize() * 2, food.getSize() * 2, this);
                }
            }

            System.out.println("SNAKESNAKE" + snakes);
            
            for (Map.Entry<String, SnakeData> snake : snakes.entrySet()) {
                SnakeData snakeData = snake.getValue();
                if (snakeData == null)
                    continue;

                Point _head = snakeData.getSnakePoint().get(0);
                double angle = snakeData.getAngle();
                g.setColor(snakeData.getColor());

                // Vẽ thân rắn
                for (Point p : snakeData.getSnakePoint()) {
                    g.fillOval(p.x - viewportX - (DOT_SIZE / 2), p.y - viewportY - (DOT_SIZE / 2), DOT_SIZE, DOT_SIZE);
                }

                int eyeSize = 10;
                int eyeOffset = 15;

                int eyeXOffset = (int) (Math.cos(angle) * eyeOffset);
                int eyeYOffset = (int) (Math.sin(angle) * eyeOffset);

                g.setColor(Color.WHITE);
                // Vẽ mắt trái và phải
                g.fillOval(_head.x - viewportX + eyeXOffset - (eyeSize / 2),
                        _head.y - viewportY + eyeYOffset - (eyeSize / 2), eyeSize, eyeSize);
                g.fillOval(_head.x - viewportX - (eyeXOffset / 3) - (eyeSize / 2),
                        _head.y - viewportY - (eyeYOffset / 3) - (eyeSize / 2), eyeSize, eyeSize);

                int pupilSize = 5;
                g.setColor(Color.BLACK);
                g.fillOval(_head.x - viewportX + eyeXOffset - (pupilSize / 2),
                        _head.y - viewportY + eyeYOffset - (pupilSize / 2), pupilSize, pupilSize);
                g.fillOval(_head.x - viewportX - (eyeXOffset / 3) - (pupilSize / 2),
                        _head.y - viewportY - (eyeYOffset / 3) - (pupilSize / 2), pupilSize, pupilSize);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                String userName = snakeData.getUserName();

                FontMetrics metrics = g.getFontMetrics(g.getFont());
                int stringWidth = metrics.stringWidth(userName);

                int x = _head.x - viewportX - stringWidth / 2;
                int y = _head.y - viewportY - 25;
                g.drawString(userName, x, y);

            }
            // Vẽ bảng xếp hạng
            drawLeaderboard(g);

            Point snakeHead = snake.get(0);

            drawScore(g);
        }
    }


    //Phương thức để vẽ bảng xếp hạng
    private void drawLeaderboard(Graphics g) {
    // Tạo một danh sách các người chơi và điểm số của họ
    Map<String, SnakeData> tmp = new HashMap<>(snakes);

    for (Map.Entry<String, SnakeData> tmpEntry : tmp.entrySet()) {
        String tmpUserId = tmpEntry.getKey(); // Lấy ID người chơi từ tmp
        SnakeData tmpData = tmpEntry.getValue(); // Lấy SnakeData từ tmp

        // Kiểm tra nếu ID này đã tồn tại trong leaderboard
        if (leaderboard.containsKey(tmpUserId)) {
            // Nếu tồn tại, cập nhật điểm
            leaderboard.get(tmpUserId).setScore(tmpData.getScore());
        } else {
            // Nếu chưa tồn tại, thêm mới vào leaderboard
            leaderboard.put(tmpUserId, tmpData);
        }
    }
    
    List<Map.Entry<String, SnakeData>> sortedLeaderboard = new ArrayList<>(leaderboard.entrySet());
    sortedLeaderboard.sort((a, b) -> Integer.compare(b.getValue().getScore(), a.getValue().getScore())); // Sắp xếp theo điểm số từ cao đến thấp

    // Thiết lập vị trí và kích thước của bảng xếp hạng
    int leaderboardWidth = 250;
    int leaderboardHeight = 50 + Math.min(10, sortedLeaderboard.size()) * 25;
    int leaderboardX = getWidth() - leaderboardWidth - 20; // Góc phải trên
    int leaderboardY = 20;

    // Vẽ nền bán trong suốt cho bảng xếp hạng
    g.setColor(new Color(0, 0, 0, 150)); // Màu đen với độ trong suốt
    g.fillRoundRect(leaderboardX - 10, leaderboardY - 10, leaderboardWidth, leaderboardHeight, 15, 15);

    // Vẽ tiêu đề
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.drawString("Bảng xếp hạng", leaderboardX + 10, leaderboardY + 10);
    leaderboardY += 30; // Tăng khoảng cách sau tiêu đề

    // Vẽ tiêu đề của các cột Rank, Tên và Điểm
    g.setFont(new Font("Arial", Font.BOLD, 16));
    g.drawString("Rank", leaderboardX + 10, leaderboardY);
    g.drawString("Tên", leaderboardX + 60, leaderboardY);
    g.drawString("Điểm", leaderboardX + 180, leaderboardY);
    leaderboardY += 20; // Tăng khoảng cách sau tiêu đề cột

    // Hiển thị tối đa 10 người chơi
    g.setFont(new Font("Arial", Font.PLAIN, 14));
    int maxEntries = Math.min(10, sortedLeaderboard.size());
    for (int i = 0; i < maxEntries; i++) {
        Map.Entry<String, SnakeData> entry = sortedLeaderboard.get(i);
        String userName = entry.getValue().getUserName();
        int score = entry.getValue().getScore();

        // Định dạng dòng để căn chỉnh cột
        String rank = (i + 1) + "";   // Thứ hạng
        String nameColumn = String.format("%-10s", userName);  // Căn trái tên với độ rộng 10
        String scoreColumn = String.format("%d", score);       // Điểm

        // Vẽ thứ hạng, tên và điểm
        g.setColor(new Color(255, 215, 0)); // Màu vàng nhạt cho văn bản
        g.drawString(rank, leaderboardX + 10, leaderboardY);
        g.drawString(nameColumn, leaderboardX + 60, leaderboardY);
        g.drawString(scoreColumn, leaderboardX + 180, leaderboardY);

        leaderboardY += 25; // Tăng khoảng cách giữa các mục
    }
}

    
    public void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Điểm: " + score, 10, 30);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    private void endGame() {
        running = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! " + "\n" + "Điểm số của bạn là: " + score);
        gameFrame.dispose();
    }

    public void updateViewport() {
        if (snake.isEmpty()) {
            return;
        }
    
        // Cập nhật viewportX và viewportY để luôn giữ đầu rắn ở giữa khung nhìn
        viewportX = head.x - (WIDTH / 2);
        viewportY = head.y - (HEIGHT / 2);
    
        // Giới hạn viewport trong phạm vi bản đồ
        viewportX = Math.max(0, Math.min(viewportX, MAP_SIZE - WIDTH));
        viewportY = Math.max(0, Math.min(viewportY, MAP_SIZE - HEIGHT));
    }
    

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

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
                    SnakeGame game = new SnakeGame(gameFrame, playerName);
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
