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
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

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

    private int viewportX = 0; // Tọa độ góc trên bên trái của viewport
    private int viewportY = 0;
    private Image backgroundImage; //ảnh nền
    Random rand = new Random();
    private Color snakeColor = new Color(rand.nextFloat(),rand.nextFloat() , rand.nextFloat()); //  tạo màu cố định cho rắn khi bắt đầu game

    Gson gson = new Gson();

    public SnakeGame(String playerName) {
        this.playerName = playerName; // Gán tên người chơi
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseMotionListener(this);

        //tải ảnh từ đường dẫn

        try {
            backgroundImage = ImageIO.read(new File("C://Users//ADMIN//OneDrive//Desktop//snakegameonline//Client//GameVisual//background.jpg")); // Đường dẫn ảnh nền
        } catch (IOException e) {
            e.printStackTrace();
        }

        startGame();
    }

    public void startGame() {
        snake.clear();
        snake.add(new Point(MAP_SIZE / 2, MAP_SIZE / 2)); // Vị trí khởi đầu của rắn

        snake.add(new Point(MAP_SIZE / 2, MAP_SIZE / 2)); // Vị trí khởi đầu của rắn

        // Đặt khung nhìn ngay tại vị trí của đầu rắn khi bắt đầu
        viewportX = MAP_SIZE / 2 - WIDTH / 2;
        viewportY = MAP_SIZE / 2 - HEIGHT / 2;

        spawnFood(100);
        running = true;
        timer = new Timer(1000 / 165, this);
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


    public class Food {
        public Point position;  // vị trí của food
        public int baseSize;  // Kích thước Food cơ bản
        public int currentSize;  // Kích thước Food hiện tại để nhấp nháy
        public Color color;     // màu sắc của food
        public Color glowColor; // Màu phát sáng (hiệu ứng)
        private boolean increasing = true; // điều khiển đốm sáng nhấm nháy

        public Food(Point position, int size, Color color, Color glowColor) {
            this.position = position;
            this.baseSize = size;
            this.currentSize = size;
            this.color = color;
            this.glowColor = glowColor;
        }
        private int blinkSpeed = 1; // Tốc độ nhấp nháy


        public void updateBlink() {
            if (increasing) {
                currentSize += blinkSpeed; //tăng kích thước theo tốc độ nhấp nháy
                if (currentSize >= baseSize + 5) { // Đốm sáng tăng lên tối đa 5 đơn vị
                    increasing = false;
                }
            } else {
                currentSize -= blinkSpeed;
                if (currentSize <= baseSize - 5) { // Đốm sáng giảm xuống tối đa 5 đơn vị
                    increasing = true;
                }
            }
        }

    }


public class Food {
    public Point position;  // vị trí của food
    public int baseSize;  // Kích thước Food cơ bản
    public int currentSize;  // Kích thước Food hiện tại để nhấp nháy
    public Color color;     // màu sắc của food
    public BufferedImage glowImage; // Hình ảnh phát sáng (hiệu ứng blur)
    private boolean increasing = true; // điều khiển đốm sáng nhấp nháy
    private int blinkSpeed = 1; // Tốc độ nhấp nháy

    // Constructor mới, thay glowColor bằng glowImage (hình ảnh với hiệu ứng blur)
    public Food(Point position, int size, Color color, BufferedImage glowImage) {
        this.position = position;
        this.baseSize = size;
        this.currentSize = size;
        this.color = color;
        this.glowImage = glowImage;
    }

    // Hàm cập nhật hiệu ứng nhấp nháy
    public void updateBlink() {
        if (increasing) {
            currentSize += blinkSpeed; //tăng kích thước theo tốc độ nhấp nháy
            if (currentSize >= baseSize + 5) { // Đốm sáng tăng lên tối đa 5 đơn vị
                increasing = false;
            }
        } else {
            currentSize -= blinkSpeed;
            if (currentSize <= baseSize - 5) { // Đốm sáng giảm xuống tối đa 5 đơn vị
                increasing = true;
            }
        }
    }

    // Hàm vẽ Food lên màn hình, vẽ cả food và glowImage (hình ảnh phát sáng)
    public void draw(Graphics2D g) {
        // Vẽ hình chính (food)
        g.setColor(color);
        g.fillOval(position.x, position.y, currentSize, currentSize);

        // Vẽ hình phát sáng mờ (glowImage)
        int glowSize = currentSize + 10; // Kích thước của đốm sáng lớn hơn một chút
        g.drawImage(glowImage, position.x - 5, position.y - 5, glowSize, glowSize, null);
    }
}

    

    private BufferedImage createBlurredImage(int size, Color color) {
        // Tạo BufferedImage để vẽ food
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Vẽ hình tròn với màu food
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);

        // Áp dụng Gaussian blur
        float[] kernel = {
                1f / 16f, 2f / 16f, 1f / 16f,
                2f / 16f, 4f / 16f, 2f / 16f,
                1f / 16f, 2f / 16f, 1f / 16f
        };
        ConvolveOp blur = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage blurredImage = blur.filter(image, null);

        g2d.dispose();
        return blurredImage;
    }

    public void spawnFood(int numberOfFoods) {
        Random rand = new Random();
        int x = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
        int y = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
        food = new Point(x, y);

        for (int i = 0; i < numberOfFoods; i++) {
            int x = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
            int y = rand.nextInt(MAP_SIZE / DOT_SIZE) * DOT_SIZE;
            int size = rand.nextInt(10) + 15;
            Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0.8f);

            BufferedImage glowImage = createBlurredImage(size, color);

            foods.add(new Food(new Point(x, y), size, color));
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
            // Di chuyển khung nhìn theo rắn
            updateViewport();



            // Vẽ đồ ăn là các "đốm sáng"
            for(Food food : foods){
            food.updateBlink();
            //vẽ lớp phát sáng mờ
            g.setColor(food.glowColor);
            g.fillOval(food.position.x - viewportX - (food.currentSize / 2), food.position.y - viewportY - (food.currentSize / 2), food.currentSize * 2, food.currentSize * 2);


            //vẽ đốm sáng chính
            g.setColor(food.color);
            g.fillOval(food.position.x - viewportX, food.position.y - viewportY, food.currentSize, food.currentSize);
            }
            // Vẽ rắn
            drawSnake(g);

        } else {
            showGameOver(g);
        }
    }


    public void drawSnake(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Kích hoạt chống răng cưa
    
        int outlineThickness = 3; // Độ dày của viền xung quanh rắn
    
        // Vẽ toàn bộ thân của rắn
        for (int i = snake.size() - 1; i >= 0; i--) { // Vẽ từ phần đuôi lên đầu
            Point p = snake.get(i);
    
            // Vẽ viền mờ quanh các khúc rắn
            g2d.setColor(new Color(0, 0, 0, 50)); // Viền đen mờ
            g2d.fillOval(p.x - viewportX - outlineThickness, p.y - viewportY - outlineThickness, 
                         DOT_SIZE + 2 * outlineThickness, DOT_SIZE + 2 * outlineThickness);
    
            // Vẽ phần thân chính của rắn bên trong viền
            g2d.setColor(snakeColor);
            g2d.fillOval(p.x - viewportX, p.y - viewportY, DOT_SIZE, DOT_SIZE);
        }
    
        // Vẽ mắt trên phần đầu của rắn
        Point head = snake.get(0); // Lấy phần đầu rắn (khúc cao nhất)
        double deltaX = mousePosition.x + viewportX - head.x;
        double deltaY = mousePosition.y + viewportY - head.y;
        double angle = Math.atan2(deltaY, deltaX);
    
        // Điều chỉnh vị trí mắt sao cho mắt luôn nằm trong thân khúc đầu tiên
        int eyeDistance = 8; // Khoảng cách từ giữa thân đến mắt, đảm bảo không vượt viền
        int eyeSize = 10; // Kích thước mắt
    
        // Tính toán vị trí của mắt trái và mắt phải, đảm bảo nằm trong viền
        int eyeOffsetX1 = (int) (Math.cos(angle - Math.PI / 6) * eyeDistance);
        int eyeOffsetY1 = (int) (Math.sin(angle - Math.PI / 6) * eyeDistance);
        int eyeOffsetX2 = (int) (Math.cos(angle + Math.PI / 6) * eyeDistance);
        int eyeOffsetY2 = (int) (Math.sin(angle + Math.PI / 6) * eyeDistance);
    
        // Vẽ mắt trắng bên trong thân đầu tiên
        g2d.setColor(Color.WHITE);
        g2d.fillOval(head.x + eyeOffsetX1 - viewportX, head.y + eyeOffsetY1 - viewportY, eyeSize, eyeSize); // Mắt trái
        g2d.fillOval(head.x + eyeOffsetX2 - viewportX, head.y + eyeOffsetY2 - viewportY, eyeSize, eyeSize); // Mắt phải
    
        // Vẽ con ngươi đen bên trong mắt trắng
        g2d.setColor(Color.BLACK);
        g2d.fillOval(head.x + eyeOffsetX1 + 2 - viewportX, head.y + eyeOffsetY1 + 2 - viewportY, 5, 5); // Con ngươi trái
        g2d.fillOval(head.x + eyeOffsetX2 + 2 - viewportX, head.y + eyeOffsetY2 + 2 - viewportY, 5, 5); // Con ngươi phải
    
        // Vẽ tên người chơi trên đầu rắn với màu trắng
        g2d.setColor(Color.WHITE); 
        g2d.setFont(new Font("Times New Roman", Font.BOLD, 20));
        g2d.drawString(playerName, head.x - viewportX, head.y - 20 - viewportY);
    }
    
    
    
    
    


    private double speedMultiplier = 0.35;

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
        head.translate((int) (DOT_SIZE * Math.cos(angle) * speedMultiplier) / 2, 
                   (int) (DOT_SIZE * Math.sin(angle) * speedMultiplier) / 2);

        client.sendSnakePosition(snake);
        Type mapType = new TypeToken<Map<String, ArrayList<Point>>>() {
        }.getType();
        snakes = gson.fromJson(client.receiveResponse(), mapType);
        System.out.println("snakes map: " + snakes);

        snake.add(0, head);

        // Kiểm tra nếu rắn ăn "đốm sáng"
        boolean ateFood = false;  // Biến để kiểm tra nếu rắn ăn food

        // Kiểm tra nếu rắn ăn "đốm sáng"
        for (int i = 0; i < foods.size(); i++) {
            Food food = foods.get(i);
            if (head.distance(food.position) < food.currentSize) {
                // Rắn đã ăn food
                ateFood = true;
                foods.remove(i);  // Loại bỏ "đốm sáng" đã ăn
                spawnFood(1);     // Sinh food mới
                break;
            }
        }

    // Nếu không ăn thì loại bỏ phần đuôi
        if (!ateFood) {
            snake.remove(snake.size() - 1);  // Chỉ loại bỏ đuôi nếu không ăn
        }

    }

    public void checkCollision() {
        Point head = snake.get(0);

        // Kiểm tra va chạm với bản thân
       

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
    
        // Tỉ lệ để khung nhìn di chuyển chậm, càng nhỏ càng chậm
        double smoothFactor = 0.05;
    
        // Di chuyển từ từ đến vị trí của đầu rắn
        viewportX += (head.x - WIDTH / 2 - viewportX) * smoothFactor;
        viewportY += (head.y - HEIGHT / 2 - viewportY) * smoothFactor;
    
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
        // Khởi tạo JFrame
        JFrame frame = new JFrame("Snake Game");

        // Tạo Panel chính
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Vẽ nền gradient
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(18, 18, 18); // màu nền tối
                Color color2 = new Color(0, 0, 0); 
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(new BorderLayout());

        // Tiêu đề "Snake Game" với gradient
        JLabel titleLabel = new JLabel("Snake Game", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(new Color(0, 255, 0)); // màu gradient xanh
        titleLabel.setOpaque(false);
        
        // Hiệu ứng Gradient cho chữ
        GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 255, 0), 300, 0, new Color(128, 0, 128));
        titleLabel.setForeground(new Color(0, 255, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Nhập nickname
        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 30));
        nameField.setBackground(new Color(48, 25, 52));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE); // con trỏ trắng
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(150, 75, 0), 2)); // Viền màu nâu
        
        // Nút Play
        JButton playButton = new JButton("Play");
        playButton.setBackground(new Color(60, 180, 75));
        playButton.setForeground(Color.WHITE);
        playButton.setFont(new Font("Arial", Font.BOLD, 30));
        playButton.setFocusPainted(false);

        // Thêm hành động cho nút Play
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText();
                if (!playerName.trim().isEmpty()) {
                    frame.dispose();
                    // Vào game sau khi nhập tên
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

        // Panel chứa nickname và button
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false); // Để nền trong suốt
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(nameField, BorderLayout.NORTH);
        inputPanel.add(playButton, BorderLayout.SOUTH);

        // Thêm vào panel chính
        panel.add(inputPanel, BorderLayout.CENTER);

        // Cài đặt frame
        frame.add(panel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // căn giữa màn hình
        frame.setVisible(true);
        
    
    }


}