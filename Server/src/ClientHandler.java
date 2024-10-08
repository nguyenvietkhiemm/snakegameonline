package Server;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class ClientHandler extends Thread {
    private static int clientCounter = 0;
    private Socket clientSocket;
    private int id;
    Gson gson = new Gson();

    // Constructor nhận socket của client
    public ClientHandler(Socket socket) {
        this.id = ++ClientHandler.clientCounter;
        this.clientSocket = socket;
    }

    public int getClientId() {
        return this.id;
    }

    @Override
    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter outToClient = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            Snake snake = new Snake(10, 10, id);
            Map<String, Integer> jsonRes = new HashMap<>();
            jsonRes.put("id", id);

            outToClient.write(gson.toJson(jsonRes) + "\n");
            outToClient.flush();

            String message;
            while ((message = inFromClient.readLine()) != null &&
                    !message.equals("exit")) {
                System.out.println("Received from client " + this.getClientId() + " : " +
                        message);

                Type mapType = new TypeToken<Map<String, Integer>>() {
                }.getType();
                // solve
                Map<String, Integer> jsonReq = gson.fromJson(message, mapType);
                Map<String, Integer> jsonResponse = new HashMap<>();

                snake.setLocation(Integer.parseInt((String) jsonReq.get("x")),
                        Integer.parseInt((String) jsonReq.get("y")));

                jsonResponse.put(String.valueOf(id), snake.location());

                outToClient.write(gson.toJson(jsonResponse) + "\n");
                outToClient.flush();
                System.out.println("sent to client" + gson.toJson(jsonResponse) +
                        "\n");
            }

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để tạo token dựa trên ID
    private String generateToken(int id) {
        String tokenBase = "ClientID:" + id; // Tạo chuỗi từ ID
        return Base64.getEncoder().encodeToString(tokenBase.getBytes()); // Mã hóa Base64
    }
}
