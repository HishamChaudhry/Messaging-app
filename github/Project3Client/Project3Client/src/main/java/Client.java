import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Date;
import java.util.function.Consumer;



public class Client extends Thread {

    Socket socketClient;
    ObjectOutputStream out;
    ObjectInputStream in;
    private String username;
    private Consumer<Serializable> callback;

    Client(Consumer<Serializable> call) {
        callback = call;
    }

    public void run() {
        try {
            socketClient = new Socket("127.0.0.1", 5555);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        } catch (Exception e) {
            callback.accept("Connection Failed");
            return;
        }

        while (true) {
            try {
                Serializable data = (Serializable) in.readObject();
                if (data instanceof Message) {
                    Message message = (Message) data;
                    
                    if (message.getMessageType() == Message.MessageType.SYSTEM) {
                        handleSystemMessage(message);
                    } 
                    else {
                        
                        callback.accept(message);
                    }
                } 
                else {
                    callback.accept(data);  
                }
            } 
            catch (Exception e) {
                callback.accept("Failed to read message from server");
                break;  
            }
        }
    }

    private void handleSystemMessage(Message message) {
        Serializable content = message.getContent();
        if (content instanceof String) {  
            String contentString = (String) content;
            switch (contentString) {
                case "username_taken":
                    callback.accept("Username is already taken, please try another.");
                    break;
                case "username_accepted":
                    callback.accept("Username registration successful.");
                    break;
                default:
                    callback.accept("Unrecognized system message: " + contentString);
                    break;
            }
        } else {
            callback.accept("Received non-string system message: " + content);
        }
    }

    public void send(Message data) {
        try {
            out.writeObject(data);
            out.flush();
        } 
        catch (IOException e) {
        	System.err.println("Error sending message: " + e.getMessage());
        }
    }
    public void setUsername(String username) {
        this.username = username;
        send(new Message(Message.MessageType.SYSTEM, username, null, "register", new Date()));
    }
    public String getUsername() {
        return this.username;
    }
    
}
