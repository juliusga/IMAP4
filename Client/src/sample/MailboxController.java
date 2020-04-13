package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class MailboxController {

    public static class Mail {
        private final SimpleStringProperty subject;
        private final SimpleStringProperty from;
        private final SimpleStringProperty size;

        private Mail(String subject, String from, String size) {
            this.subject = new SimpleStringProperty(subject);
            this.from = new SimpleStringProperty(from);
            this.size = new SimpleStringProperty(size);
        }

        public String getSubject() {
            return subject.get();
        }
        public void setSubject(String fName) {
            subject.set(fName);
        }

        public String getFrom() {
            return from.get();
        }
        public void setFrom(String fName) {
            from.set(fName);
        }

        public String getSize() {
            return size.get();
        }
        public void setSize(String fName) {
            size.set(fName);
        }
    }

    ObservableList<Mail> data;

    @FXML
    private ListView<String> inboxList;
    @FXML
    private TableView<Mail> emails;
    @FXML
    private TableColumn<Mail, String> subjectColumn;
    @FXML
    private TableColumn<Mail, String> fromColumn;
    @FXML
    private TableColumn<Mail, String> sizeColumn;
    @FXML
    private Label userName;
    @FXML
    private Button selectInboxBtn;
    @FXML
    private Button viewLetterBtn;

    private Controller controller;

    public void setValues(Controller controller)
    {
        this.controller = controller;
        this.userName.setText    (controller.getUserNameStr());
        try {
            refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
        viewLetterBtn.setDisable(true);
    }

    public void refresh() throws IOException {
        inboxList.getItems().clear();
        //controller.increaseMessageAttribute();
        controller.setSendStr(controller.getFullMessageAttribute() + " LSUB * *");
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
            int start = i;
            int end = i;
            String inboxName;
            while (i < controller.getReceiveStr().length())
            {
                while (end < controller.getReceiveStr().length()
                        && controller.getReceiveStr().charAt(end) != ' ')
                {
                    end++;
                }
                inboxName = controller.getReceiveStr().substring(start, end);
                inboxList.getItems().add(inboxName);
                start = ++end;
                i = start;
            }
        }
    }

    @FXML
    void newInboxPressed(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("new_inbox.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 183);
            Stage stage = new Stage();
            stage.setTitle("Create inbox");
            NewInboxController newInboxController = (NewInboxController)fxmlLoader.getController();
            newInboxController.start(controller);
            stage.getIcons().add(new Image("file:src/ICON_client.png"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logoutPressed(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are your sure you want to logout?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) controller.sendLogout();
    }

    @FXML
    void deleteInbox(ActionEvent event) {
        if (inboxList.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select the inbox in the Inboxes table!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are your sure you want to delete inbox " + inboxList.getSelectionModel().getSelectedItem()
                        + "?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK)
        {
            String send = controller.getFullMessageAttribute() + " DELETE "
                    + inboxList.getSelectionModel().getSelectedItem();
            controller.setSendStr(send);
            try {
                controller.getCommunication().send(controller.getSendStr());
                controller.setReceiveStr(controller.getCommunication().receive());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (controller.responseAnalyzer() == ResponseHandler.OK)
            {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                        "Inbox " + inboxList.getSelectionModel().getSelectedItem()
                                + " successfully deleted.", ButtonType.OK);
                alert1.showAndWait();
                try {
                    refresh();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else return;
    }

    @FXML
    private void rename()
    {
        if (inboxList.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select the inbox in the Inboxes table!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("rename.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 183);
            Stage stage = new Stage();
            stage.setTitle("Rename inbox");
            RenameController renameController = (RenameController) fxmlLoader.getController();
            renameController.start(controller, inboxList.getSelectionModel().getSelectedItem());
            stage.getIcons().add(new Image("file:src/ICON_client.png"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void viewAllPressed(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("viewAll.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 370, 330);
            Stage stage = new Stage();
            stage.setTitle("View all");
            ViewAllController viewAllController = (ViewAllController) fxmlLoader.getController();
            viewAllController.start(controller);
            stage.getIcons().add(new Image("file:src/ICON_client.png"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void selectInbox(ActionEvent event) {
        if (controller.getCurrentState() == controller.AUTHENTICATED)
        {
            if (inboxList.getSelectionModel().getSelectedItem() == null){
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Please select the inbox in the Inboxes table!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            else selectInbox();
        }
        else if (controller.getCurrentState() == controller.SELECTED) {
            closeInbox();
        }
    }

    private void selectInbox()
    {
        if (inboxList.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select the inbox in the Inboxes table!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        String send = controller.getFullMessageAttribute() + " SELECT "
                + inboxList.getSelectionModel().getSelectedItem();
        controller.setSendStr(send);
        try {
            controller.getCommunication().send(controller.getSendStr());
            controller.setReceiveStr(controller.getCommunication().receive());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (controller.responseAnalyzer() == ResponseHandler.OK)
        {
            controller.setCurrentState(controller.SELECTED);
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Inbox selected successfully", ButtonType.OK);
            alert.showAndWait();
            selectInboxBtn.setText("Close inbox");
            int start = 8;
            int end = start;
            String subject;
            String from;
            String size;
            while (end < controller.getReceiveStr().length()){
                while (controller.getReceiveStr().charAt(end) != '@') end++;
                subject = controller.getReceiveStr().substring(start, end);
                start = ++end;
                while (controller.getReceiveStr().charAt(end) != '@') end++;
                from = controller.getReceiveStr().substring(start, end);
                start = ++end;
                while (controller.getReceiveStr().charAt(end) != '|') end++;
                size = controller.getReceiveStr().substring(start, end);
                start = ++end;
                if(data == null) data = FXCollections.observableArrayList((new Mail(subject, from, size)));
                else data.add(new Mail(subject, from, size));
            }
            subjectColumn.setCellValueFactory(new PropertyValueFactory<Mail,String>("subject"));
            fromColumn.setCellValueFactory(new PropertyValueFactory<Mail,String>("from"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<Mail,String>("size"));
            emails.setItems(data);
            viewLetterBtn.setDisable(false);
        }
    }


    private void closeInbox()
    {
        String send = controller.getFullMessageAttribute() + " CLOSE";
        controller.setSendStr(send);
        try {
            controller.getCommunication().send(controller.getSendStr());
            controller.setReceiveStr(controller.getCommunication().receive());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (controller.responseAnalyzer() == ResponseHandler.OK)
        {
            controller.setCurrentState(controller.AUTHENTICATED);
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Inbox closed successfully", ButtonType.OK);
            alert.showAndWait();
            selectInboxBtn.setText("Select inbox");
            data.clear();
            emails.getItems().clear();
        }
    }

    @FXML
    void viewSelectedLetter(ActionEvent event) {
        if(emails.getSelectionModel().getSelectedItem() == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select an email.", ButtonType.OK);
            alert.showAndWait();
        }
        Mail mail = emails.getSelectionModel().getSelectedItem();
        String send = controller.getFullMessageAttribute() + " FETCH |" + mail.getSubject() + "@" +
                mail.getFrom() + "@" + mail.getSize();
        controller.setSendStr(send);
        try {
            controller.getCommunication().send(controller.getSendStr());
            controller.setReceiveStr(controller.getCommunication().receive());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (controller.responseAnalyzer() == ResponseHandler.OK)
        {
            controller.setCurrentState(controller.SELECTED);
            Alert alert = new Alert(Alert.AlertType.NONE,
                    controller.getReceiveStr().substring(8), ButtonType.OK);
            alert.showAndWait();
        }
    }
}
