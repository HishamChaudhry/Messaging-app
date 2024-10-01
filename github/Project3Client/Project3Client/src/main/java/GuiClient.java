import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.Date;

public class GuiClient extends Application {

    TextField c1, usernameField;
    Button b1, setUsernameButton;
    HashMap<String, Scene> sceneMap;
    VBox clientBox;
    Client clientConnection;
    ListView<String> listItems2;
    ComboBox<String> userSelector;
    private ArrayList<String> activeUsers = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        clientConnection = new Client(data -> {
            Platform.runLater(() -> {
                System.out.println("Received data: " + data);
                if (data instanceof Message) {
                    Message msg = (Message) data;
                    System.out.println("Message type: " + msg.getMessageType());
                    listItems2.getItems().add(data.toString());
                    if (msg.getMessageType() == Message.MessageType.USER_LIST) {
                        ArrayList<String> userList = (ArrayList<String>) msg.getContent();
                        System.out.println("Updating user list with: " + userList);
                        updateComboBoxWithUsernames(userList);
                    }
                } 
                else if (data instanceof String) {
                    String response = (String) data;
                    if (response.equals("Username is already taken, please try another.")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, response);
                        alert.showAndWait();
                        usernameField.setDisable(false);
                        setUsernameButton.setDisable(false);
                        usernameField.clear();
                    } 
                    else if (response.equals("Username registration successful.")) {
                        usernameField.setDisable(true);
                        setUsernameButton.setDisable(true);
                    } else {
                        listItems2.getItems().add(response);
                    }
                }
            });
        });

        clientConnection.start();
        listItems2 = new ListView<>();
        userSelector = new ComboBox<>();
        userSelector.getItems().add("All Users");
        userSelector.getSelectionModel().selectFirst();

        usernameField = new TextField();
        usernameField.setPromptText("Enter Username");
        setUsernameButton = new Button("Set Username");
        setUsernameButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                clientConnection.setUsername(username);
            } 
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Username cannot be empty. Please enter a valid username.");
                alert.showAndWait();
            }
        });

        c1 = new TextField();
        b1 = new Button("Send");
        b1.setOnAction(e -> sendMessage());

        sceneMap = new HashMap<>();
        sceneMap.put("client", createClientGui());

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setScene(sceneMap.get("client"));
        primaryStage.setTitle("Client");
        primaryStage.show();
    }

    private void updateComboBoxWithUsernames(ArrayList<String> usernames) {
        userSelector.getItems().clear();
        userSelector.getItems().add("All Users");
        userSelector.getItems().addAll(usernames);
        userSelector.getSelectionModel().selectFirst();
        activeUsers.clear();
        activeUsers.addAll(usernames);
    }

    private void sendMessage() {
        String messageText = c1.getText();
        if (messageText.isEmpty()) return;
        String recipient = userSelector.getValue();
        Message message;
        if ("All Users".equals(recipient)) {
            message = new Message(Message.MessageType.BROADCAST, clientConnection.getUsername(), null, messageText, new Date());
        } 
        else if (activeUsers.contains(recipient)) {
            message = new Message(Message.MessageType.PRIVATE, clientConnection.getUsername(), recipient, messageText, new Date());
        } 
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a valid thing.");
            alert.showAndWait();
            return;
        }
        clientConnection.send(message);
        c1.clear();
    }

    public Scene createClientGui() {
        HBox userInput = new HBox(10, usernameField, setUsernameButton);
        HBox messageInput = new HBox(10, c1, userSelector, b1);
        clientBox = new VBox(10, userInput, messageInput, listItems2);
        clientBox.setStyle("-fx-background-color: blue; -fx-font-family: 'serif';");
        return new Scene(clientBox, 500, 400);
    }
}
