package Server;
import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(60000)) {
            System.out.println("Server is running...");

            while (true) {
                Socket connectionSocket = serverSocket.accept();
                System.out.println("New client connected");
                
                // multi thread
                ClientHandler clientHandler = new ClientHandler(connectionSocket);
                clientHandler.start();
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
