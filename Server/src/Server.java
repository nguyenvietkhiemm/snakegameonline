package Server.src;

import java.io.*;
import java.net.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Server {
    private static int clientCounter = 0;
    public static final Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(60000)) {
            System.out.println("UDP Server is running...");

            byte[] receiveBuffer = new byte[1024];

            Thread senderThread = new Thread(new ClientSender(serverSocket));
            senderThread.start();

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                String clientKey = clientAddress.toString() + ":" + clientPort;

                ClientHandler clientHandler = clients.get(clientKey);
                
                if (clientHandler == null) {
                    clientHandler = new ClientHandler(clientAddress, clientPort, serverSocket, ++clientCounter);
                    clients.put(clientKey, clientHandler);
                    clientHandler.handleClientMessageFirst(message);
                    
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                    
                    System.out.println("New client connected: " + clientKey);
                } 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
