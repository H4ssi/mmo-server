package mmo.server.message;

public class Chat implements Message {
    private String message;

    public Chat() {
    }

    public Chat(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
