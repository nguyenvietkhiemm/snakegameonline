package Client.src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.awt.*;
import java.util.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // Địa chỉ IP của server
    private static final int SERVER_PORT = 60000;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    Gson gson = new Gson();

    public Client() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            System.out.println("Connected to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm gửi vị trí rắn, màu rắn, tên rắn lên server
    public void sendSnakeData(SnakeData snakeData) {
        try {
            String message = gson.toJson(snakeData);
            byte[] sendBuffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Hàm gửi vị trí snake lên server
    public void sendSnakeDirection(Point snakeHead, Point mousePosition) {
        try {
            double deltaX = mousePosition.x - snakeHead.x;
            double deltaY = mousePosition.y - snakeHead.y;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance < 50) {
                return;
            }
            double angle = Math.atan2(deltaY, deltaX);
            angle = Math.round(angle * 1000.0) / 1000.0;
            Map<String, Double> messageMap = new HashMap<>();
            messageMap.put(SnakeGame.id, angle);
            
            String message = gson.toJson(messageMap);
            byte[] sendBuffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
            System.out.println("Sent angle: " + angle);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Nhận phản hồi từ server
    public String receiveResponse() {
        try {
            byte[] receiveBuffer = new byte[20480];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            return new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void closeConnection() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        // Nhận phản hồi từ server
        String response = client.receiveResponse();
        System.out.println("Server response: " + response);

        client.closeConnection();
    }
}
