package Server.src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class ClientHandler {
    private InetAddress clientAddress;
    private int clientPort;
    private DatagramSocket socket;
    private int id;
    private ArrayList<Point> snake = new ArrayList<>();
    private static Gson gson = new Gson();

    public ClientHandler(InetAddress clientAddress, int clientPort, DatagramSocket socket, int id) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.socket = socket;
    }

    public void handleClientMessage(String message) {
        try {
            Map<String, Integer> jsonRes = new HashMap<>();
            jsonRes.put("id", id);

            // Gửi lại ID cho client
            sendMessage(gson.toJson(jsonRes));

            // Xử lý thông điệp nhận từ client
            try {
                Type snakeDataType = new TypeToken<SnakeData>() {}.getType();
                SnakeData snakeData = gson.fromJson(message, snakeDataType);

                if (snakeData != null) {
                    // Cập nhật snake của người chơi
                    Match.updateSnake(String.valueOf(id), snakeData);
                }
            } catch (Exception e) {
                try {
                    Type pointListType = new TypeToken<ArrayList<Point>>() {}.getType();
                    ArrayList<Point> newPoints = gson.fromJson(message, pointListType);

                    if (newPoints != null) {
                        SnakeData existingSnakeData = Match.getSnakeDataById(String.valueOf(id));
                        if (existingSnakeData != null) {
                            existingSnakeData.setSnakePoint(newPoints);
                            Match.updateSnake(String.valueOf(id), existingSnakeData);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Invalid message format received: " + message);
                }
            }

            // Gửi tất cả thông tin rắn hiện có cho client
            Map<String, SnakeData> jsonResponse = Match.getSnakes();
            Type responseType = new TypeToken<Map<String, SnakeData>>() {}.getType();
            sendMessage(gson.toJson(jsonResponse, responseType));

            System.out.println("Sent to client: " + gson.toJson(jsonResponse, responseType) + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }
}
