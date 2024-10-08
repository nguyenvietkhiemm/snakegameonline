package Client;

import java.io.*;
import java.net.*;
import java.awt.Point;
import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map
import com.google.gson.Gson;
import java.util.ArrayList;
import java.awt.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // Địa chỉ IP của server (localhost cho thử nghiệm)
    private static final int SERVER_PORT = 60000;

    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;

    Gson gson = new Gson();

    public Client() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm gửi vị trí rắn lên server
    public void sendSnakePosition(ArrayList<Point> snake) {
        try {
            out.write(gson.toJson(snake) + "\n");
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Nhận phản hồi từ server
    public String receiveResponse() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        String response = client.receiveResponse();
        System.out.println("Server response: " + response);

        client.closeConnection();
    }
}
