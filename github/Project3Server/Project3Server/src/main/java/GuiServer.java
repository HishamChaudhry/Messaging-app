import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.geometry.Insets;

public class GuiServer extends Application {

    HashMap<String, Scene> sceneMap;
    Server serverConnection;

    ListView<String> listItems; 

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        serverConnection = new Server(data -> {
            Platform.runLater(() -> {
                if (data instanceof Message) {
                    Message msg = (Message) data;
                    listItems.getItems().add(formatMessageDisplay(msg)); 
                } 
                else {
                    listItems.getItems().add(data.toString()); 
                }
            });
        });

        listItems = new ListView<>();

        sceneMap = new HashMap<>();
        sceneMap.put("server", createServerGui());

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setScene(sceneMap.get("server"));
        primaryStage.setTitle("Server Area");
        primaryStage.show();
    }

    private String formatMessageDisplay(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(message.getTimestamp()).append("] ");
        switch (message.getMessageType()) {
            case PRIVATE:
                sb.append("Private from ").append(message.getSenderId()).append(" to ").append(message.getReceiverId());
                break;
            case GROUP:
                sb.append("Group (").append(message.getGroupId()).append(") from ").append(message.getSenderId());
                break;
            case BROADCAST:
                sb.append("Broadcast from ").append(message.getSenderId());
                break;
            case SYSTEM:
                sb.append("[SYSTEM]");
                break;
            default:
                sb.append("[UNKNOWN TYPE]");
        }
        sb.append(": ").append(message.getContent());
        return sb.toString();
    }

    public Scene createServerGui() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(70));
        pane.setStyle("-fx-background-color: coral; -fx-font-family: 'serif'");

        pane.setCenter(listItems); 
        return new Scene(pane, 600, 500);
    }
}
