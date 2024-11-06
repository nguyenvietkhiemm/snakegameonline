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

public class ClientHandler implements Runnable {
    private InetAddress clientAddress;
    private int clientPort;
    private DatagramSocket socket;
    private int id;
    private ArrayList<Point> snake = new ArrayList<>();
    private static Gson gson = new Gson();
    private final int MAP_SIZE = 5000;
    private final int DOT_SIZE = 35;
    private final double speedMultiplier = 0.5;

    public ClientHandler(InetAddress clientAddress, int clientPort, DatagramSocket socket, int id) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.socket = socket;

        connected();
    }

    void connected() {
        try {
            Map<String, Integer> jsonRes = new HashMap<>();
            jsonRes.put("id", id);

            sendMessage(gson.toJson(jsonRes));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[1024];
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.printf("RECEIVED from client %d: %s%n", id, message);
                handleClientMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void handleClientMessageFirst(String message) {
        try {
            Type snakeDataType = new TypeToken<SnakeData>() {}.getType();
            SnakeData snakeData = gson.fromJson(message, snakeDataType);
            snake = snakeData.getSnakePoint();

            if (snakeData != null) {
                Match.updateSnake(String.valueOf(id), snakeData);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleClientMessage(String message) {
        try {
            double angle = Double.parseDouble(message);

            SnakeData existingSnakeData = Match.getSnakeDataById(String.valueOf(id));
            if (existingSnakeData != null) {
                existingSnakeData.setAngle(angle);
            }
            
        } catch (Exception ex) {
            System.err.println("Invalid message format received: " + message);
        }
    }

    public void sendMessage(String message) throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }
}
