import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
/*
* Clicker: A: I really get it    B: No idea what you are talking about
* C: kind of following
*/

public class Server {
    private int count = 1;
    private CopyOnWriteArrayList<ClientThread> clients = new CopyOnWriteArrayList<>();
    private Set<String> registeredUsernames = new HashSet<>();
    private Consumer<Serializable> callback;

    public Server(Consumer<Serializable> call) {
        this.callback = call;
        new TheServer().start();
    }

    private void broadcastUserList() {
        ArrayList<String> usernames = new ArrayList<>(registeredUsernames);
        Message userListMessage = new Message(Message.MessageType.USER_LIST, "server", null, usernames, new Date());
        for (ClientThread client : clients) {
            client.sendMessage(userListMessage);
        }
    }

    class TheServer extends Thread {
        public void run() {
            try (ServerSocket mysocket = new ServerSocket(5555)) {
                System.out.println("Server is waiting for a client!");
                while (true) {
                    Socket socket = mysocket.accept();
                    ClientThread client = new ClientThread(socket, count);
                    clients.add(client);
                    client.start();
                    callback.accept("Client has connected to server: client #" + count);
                    count++;
                }
            } catch (IOException e) {
                callback.accept("Server socket did not launch");
            }
        }
    }

    class ClientThread extends Thread {
        Socket connection;
        int clientId;
        ObjectInputStream in;
        ObjectOutputStream out;
        String username;

        ClientThread(Socket s, int count) {
            this.connection = s;
            this.clientId = count;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                connection.setTcpNoDelay(true);

                while (true) {
                    Message message = (Message) in.readObject();
                    processMessage(message); 
                }
            } catch (Exception e) {
                System.out.println("Error handling client #" + clientId);
                clients.remove(this);
                registeredUsernames.remove(username); 
                broadcastUserList(); 
                updateClients(new Message(Message.MessageType.SYSTEM, "server", null, "Client #" + clientId + " has left the server!", new Date()));
            }
        }

        private boolean registerUsername(String username) {
            synchronized (registeredUsernames) { 
                if (registeredUsernames.contains(username)) {
                    sendMessage(new Message(Message.MessageType.SYSTEM, "server", username, "username_taken", new Date()));
                    return false;
                } 
                else {
                    registeredUsernames.add(username);
                    this.username = username;
                    sendMessage(new Message(Message.MessageType.SYSTEM, "server", username, "username_accepted", new Date()));
                    broadcastUserList(); 
                    return true;
                }
            }
        }

        private void processMessage(Message message) {
            switch (message.getMessageType()) {
                case PRIVATE:
                    sendPrivateMessage(message);
                    break;
                case BROADCAST:
                    updateClients(message);
                    break;
                case SYSTEM:
                    if ("register".equals(message.getContent())) {
                        registerUsername(message.getSenderId());
                    }
                    break;
                default:
                    System.out.println("Unknown message type received");
            }
        }

        private void sendPrivateMessage(Message message) {
            clients.stream()
                .filter(c -> c.username.equals(message.getReceiverId()))
                .findFirst()
                .ifPresent(client -> client.sendMessage(message));
        }

        private void updateClients(Message message) {
            for (ClientThread client : clients) {
                client.sendMessage(message);
            }
        }

        private void sendMessage(Message message) {
            try {
                out.writeObject(message);
            } catch (IOException e) {
                System.out.println("Failed to send message to client #" + clientId);
            }
        }
    }
}
