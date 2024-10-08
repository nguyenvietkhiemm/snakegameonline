package Server;

import java.util.HashMap; // Nhập khẩu HashMap
import java.util.Map; // Nhập khẩu Map

public class Snake {
    private int x;
    private int y;
    private int id;
    
    // Constructor
    public Snake(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    // Getters and setters
    public Map<String, Object> location(){
        return new HashMap<String, Object>() {{
            put("x", x);
            put("y", y);
        }};
    }

    public void setLocation(int x, int y){
        this.x = x;
        this.y = y;
        return;
    }

    public int getId() {
        return this.id;
    }
}
