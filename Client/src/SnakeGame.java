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
    private final int MAP_SIZE = 5000;
    private final int DOT_SIZE = 35;

    private Point mousePosition = new Point(MAP_SIZE / 5 - WIDTH / 2, MAP_SIZE / 5 - HEIGHT / 2);
    private ArrayList<Point> snake = new ArrayList<>();
    private Point head = new Point();
    Map<String, SnakeData> snakes = new HashMap<>();

    private boolean running = false;
    private Timer timer;
    private String id = null;
    private String playerName = "";

    private ArrayList<Food> foods = new ArrayList<>();
    private int score = 0;
    private int viewportX = 0;
    private int viewportY = 0;
    private Image backgroundImage;

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

        SnakeData snakeData = new SnakeData(snake, playerName, snakeColor);
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

    private void sendSnakeMovingLoop() {
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
        while (running) {
            try {
                String response = client.receiveResponse();
                synchronized (this) {
                    Type mapType = new TypeToken<Map<String, SnakeData>>() {
                    }.getType();
                    snakes = gson.fromJson(response, mapType);
                    snake = snakes.get(id).getSnakePoint();
                    if (snake == null) {
                        running = false;
                        endGame();
                        break;
                        
                    }
                    head = snake.get(0);
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

    private void receiveResponsesLoopFoods() {
        while (running) {
            try {


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

                // Vẽ thức ăn chính
                g.setColor(food.getColor());
                g.fillOval(posX, posY, food.getSize(), food.getSize());
            }
            System.out.println("SNAKESNAKE" + snakes);
            for (Map.Entry<String, SnakeData> snake : snakes.entrySet()) {
                SnakeData snakeData = snake.getValue();
                if (snakeData == null)
                    continue;

                Point head = snakeData.getSnakePoint().get(0);
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
                g.fillOval(head.x - viewportX + eyeXOffset - (eyeSize / 2),
                        head.y - viewportY + eyeYOffset - (eyeSize / 2), eyeSize, eyeSize);
                g.fillOval(head.x - viewportX - (eyeXOffset / 3) - (eyeSize / 2),
                        head.y - viewportY - (eyeYOffset / 3) - (eyeSize / 2), eyeSize, eyeSize);

                int pupilSize = 5;
                g.setColor(Color.BLACK);
                g.fillOval(head.x - viewportX + eyeXOffset - (pupilSize / 2),
                        head.y - viewportY + eyeYOffset - (pupilSize / 2), pupilSize, pupilSize);
                g.fillOval(head.x - viewportX - (eyeXOffset / 3) - (pupilSize / 2),
                        head.y - viewportY - (eyeYOffset / 3) - (pupilSize / 2), pupilSize, pupilSize);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                String userName = snakeData.getUserName();

                FontMetrics metrics = g.getFontMetrics(g.getFont());
                int stringWidth = metrics.stringWidth(userName);

                int x = head.x - viewportX - stringWidth / 2;
                int y = head.y - viewportY - 25;
                g.drawString(userName, x, y);
            }

            int diameter = 10;
            g.fillOval(mousePosition.x - diameter / 2, mousePosition.y - diameter / 2, diameter, diameter);
            Point snakeHead = snake.get(0);
            g.drawLine(head.x - viewportX, head.y - viewportY, mousePosition.x, mousePosition.y);

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
        repaint();
    }

    private void endGame() {
        running = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! " + "\n" + "Điểm số của bạn là: " + score);
        gameFrame.dispose();
    }

    public void updateViewport() {
        if (snake.isEmpty())
            return;

        double smooth = 0.05;
        viewportX += (head.x - viewportX - (WIDTH / 2)) * smooth;
        viewportY += (head.y - viewportY - (HEIGHT / 2)) * smooth;

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
