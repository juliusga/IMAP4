package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class NewInboxController {

    @FXML
    private TextField name;

    @FXML
    private Button create;

    Controller controller;

    @FXML
    void createButtonPressed(ActionEvent event) {
        if(name.getText().isEmpty() || name.getText().contains(" ")){
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Inbox name is incorrect.", ButtonType.OK);
            alert.showAndWait();
            name.clear();
        }
        else if(name.getText().length() > 15) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Inbox name is too long.", ButtonType.OK);
            alert.showAndWait();
            name.clear();
        }
        else {
            try {
                //controller.increaseMessageAttribute();
                controller.getCommunication().send(controller.getFullMessageAttribute() + " CREATE " + name.getText());
                controller.setReceiveStr(controller.getCommunication().receive());
                if (controller.responseAnalyzer() == ResponseHandler.OK)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Inbox <" + name.getText() + "> successfully created.", ButtonType.OK);
                    alert.showAndWait();
                    controller.getMailboxController().refresh();
                }
                else if (controller.responseAnalyzer() == ResponseHandler.NO)
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Inbox with this name cannot be created.", ButtonType.OK);
                    alert.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = (Stage) name.getScene().getWindow();
            stage.close();
        }
    }

    public void start(Controller controller)
    {
        this.controller = controller;
    }

}
