package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class ViewAllController {

    public static class Inbox {
        private final SimpleStringProperty name;
        private final SimpleStringProperty subscription;

        private Inbox(String name, String subscribtion) {
            this.name = new SimpleStringProperty(name);
            this.subscription = new SimpleStringProperty(subscribtion);
        }

        public String getName() {
            return name.get();
        }
        public void setName(String fName) {
            name.set(fName);
        }

        public String getSubscription() {
            return subscription.get();
        }
        public void setSubscription(String fName) {
            subscription.set(fName);
        }
    }

    @FXML
    private TableView<Inbox> inboxList;

    @FXML
    private TableColumn<Inbox, String> name;

    @FXML
    private TableColumn<Inbox, String> sub;

    @FXML
    private Button button;

    private Controller controller;

    ObservableList<Inbox> data;

    @FXML
    void buttonPressed(ActionEvent event) {
        if(inboxList.getSelectionModel().getSelectedItem() == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select an inbox.", ButtonType.OK);
            alert.showAndWait();
        }
        else
        {
            Inbox inbox = inboxList.getSelectionModel().getSelectedItem();
            String sub;
            if (inbox.getSubscription().equals("+")) sub = "unsubscribe";
            else sub = "subscribe";
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Are you sure you want to " + sub + " inbox " + inbox.getName() + "?",
                    ButtonType.OK, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK)
            {
                if (sub.equals("unsubscribe")) {
                    String sendStr = controller.getFullMessageAttribute() + " UNSUBSCRIBE "
                            + inbox.getName();
                    controller.setSendStr(sendStr);
                    try {
                        controller.getCommunication().send(controller.getSendStr());
                        controller.getCommunication().receive();
                        if (controller.responseAnalyzer() == ResponseHandler.OK){
                            Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                                    "You successfully unsubscribed the inbox " +
                                            inbox.getName(), ButtonType.OK);
                            alert1.showAndWait();
                            refresh();
                            controller.getMailboxController().refresh();
                        }
                        else {
                            Alert alert1 = new Alert(Alert.AlertType.ERROR,
                                    "Server encountered unexpected error");
                            alert1.showAndWait();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    String sendStr = controller.getFullMessageAttribute() + " SUBSCRIBE "
                            + inbox.getName();
                    controller.setSendStr(sendStr);
                    try {
                        controller.getCommunication().send(controller.getSendStr());
                        controller.getCommunication().receive();
                        if (controller.responseAnalyzer() == ResponseHandler.OK){
                            Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                                    "You successfully subscribed the inbox " +
                                            inbox.getName(), ButtonType.OK);
                            alert1.showAndWait();
                            refresh();
                            controller.getMailboxController().refresh();
                        }
                        else {
                            Alert alert1 = new Alert(Alert.AlertType.ERROR,
                                    "Server encountered unexpected error");
                            alert1.showAndWait();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else return;
        }
    }

    void refresh() throws IOException {
        inboxList.getItems().clear();
        if(data != null) data.clear();
        controller.setSendStr(controller.getFullMessageAttribute() + " LIST * *");
        controller.getCommunication().send(controller.getSendStr());
        controller.setReceiveStr(controller.getCommunication().receive());
        if(controller.responseAnalyzer() == ResponseHandler.OK)
        {
            if (controller.getReceiveStr().length() <= 7) return;
            int i = 5;
            while(controller.getReceiveStr().charAt(i) != ' ')
            {
                i++;
            }
            i++;
            int startingP = i;
            String inboxName;
            String status;
            while (i < controller.getReceiveStr().length())
            {
                while (controller.getReceiveStr().charAt(i) != '/') i++;
                inboxName = controller.getReceiveStr().substring(startingP, i);
                if (controller.getReceiveStr().charAt(i+1) == ('s')) status = "+";
                else status = "-";
                i = i+3;
                if(data == null) data = FXCollections.observableArrayList((new Inbox(inboxName, status)));
                else data.add(new Inbox(inboxName, status));
                startingP = i;
            }
        }
        name.setCellValueFactory(new PropertyValueFactory<Inbox,String>("name"));
        sub.setCellValueFactory(new PropertyValueFactory<Inbox,String>("subscription"));
        inboxList.setItems(data);
    }

    public void start(Controller controller){
        this.controller = controller;
        try {
            refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
