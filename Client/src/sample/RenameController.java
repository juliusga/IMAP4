package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RenameController {

    @FXML
    private TextField name;

    @FXML
    private TextField currentName;

    @FXML
    private Button rename;

    private Controller controller;
    String inbox;

    @FXML
    void renameButtonPressed(ActionEvent event) {
        //controller.increaseMessageAttribute();
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
            String send = controller.getFullMessageAttribute() + " RENAME "
                    + currentName.getText() + " " + name.getText();
            controller.setSendStr(send);
            try {
                controller.getCommunication().send(controller.getSendStr());
                controller.setReceiveStr(controller.getCommunication().receive());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (controller.responseAnalyzer() == ResponseHandler.OK) {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                        "Inbox " + currentName.getText() + " renamed to " + name.getText(), ButtonType.OK);
                alert1.showAndWait();
                Stage stage = (Stage) name.getScene().getWindow();
                stage.close();
                try {
                    controller.getMailboxController().refresh();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(controller.responseAnalyzer() == ResponseHandler.NO)
            {
                Alert alert1 = new Alert(Alert.AlertType.ERROR,
                        "Inbox with the name " + currentName.getText() + " already exists!"
                        , ButtonType.OK);
                alert1.showAndWait();
                name.clear();
            }
        }
    }

    public void start(Controller controller, String inbox)
    {
        this.controller = controller;
        this.inbox = inbox;
        currentName.setText(inbox);
    }

}
