package Server.src;

import java.io.*;
import java.net.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    private static int clientCounter = 0;

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(60000)) {
            System.out.println("UDP Server is running...");

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                // Nhận gói tin từ client
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received from client: " + message);

                // Lấy địa chỉ và cổng của client
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Khởi tạo ClientHandler và xử lý dữ liệu
                ClientHandler clientHandler = new ClientHandler(clientAddress, clientPort, serverSocket,
                        ++clientCounter);
                clientHandler.handleClientMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
