package Client.src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import com.google.gson.Gson;
import java.awt.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // Địa chỉ IP của server
    private static final int SERVER_PORT = 60000;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private Gson gson = new Gson();

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

    // Hàm gửi vị trí rắn (mảng điểm) lên server
    public void sendSnakeLocation(ArrayList<Point> snake) {
        try {
            String message = gson.toJson(snake);
            byte[] sendBuffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Nhận phản hồi từ server
    public String receiveResponse() {
        try {
            byte[] receiveBuffer = new byte[1024];
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
