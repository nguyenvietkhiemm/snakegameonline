package Server.src;

import java.net.DatagramSocket;
import java.util.Map;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

public class ClientSender implements Runnable {
    private DatagramSocket socket;
    private final Gson gson = new Gson();

    public ClientSender(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            if (!Server.clients.isEmpty()) {
                try {
                    Map<String, SnakeData> jsonResponse = Match.getSnakes();
                    Type responseType = new TypeToken<Map<String, SnakeData>>() {}.getType();
                    System.out.println("Sent to clients: " + gson.toJson(jsonResponse, responseType) + "\n");

                    synchronized (Server.clients) {
                        for (Map.Entry<String, ClientHandler> entry : Server.clients.entrySet()) {
                            ClientHandler clientHandler = entry.getValue();
                            clientHandler.sendMessage(gson.toJson(jsonResponse, responseType));
                        }
                    }
                    Thread.sleep(1000/165);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
