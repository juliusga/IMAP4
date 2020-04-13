package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    public final static int WIDTH  = 500;
    public final static int HEIGHT = 250;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        Parent root = (Parent)loader.load();
        Controller controller = (Controller)loader.getController();
        primaryStage.getIcons().add(new Image("file:src/ICON_client.png"));
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(controller.getCurrentState() != 0 &&
                        controller.getCurrentState() != controller.LOGOUT)
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "Client will disconnect now", ButtonType.OK);
                    alert.showAndWait();
                    event.consume();
                    controller.sendLogout();
                }
                else {
                    Platform.exit();
                    System.exit(0);
                }
            }
        });
        primaryStage.setTitle("Client");
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setResizable(false);
        primaryStage.show();
        controller.setRoot(root);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
