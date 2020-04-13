package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Controller {

    @FXML
    public TextField ipAdress;
    @FXML
    private Label status;
    @FXML
    private Button connect;
    @FXML
    private Pane connection;
    @FXML
    private Pane login;
    @FXML
    private Button loginButton;
    @FXML
    private TextField userName;
    @FXML
    private PasswordField password;

    public static final int NOT_AUTHENTICATED = 1;
    public static final int AUTHENTICATED = 2;
    public static final int SELECTED = 3;
    public static final int LOGOUT = 4;

    private Socket socket;
    private final static int PORT = 60000;
    private String uid;
    private int messageAttribute;
    private String receiveStr, sendStr;
    private int currentState = 0;
    private Parent root;
    private Communication communication;
    private String userNameStr;
    private MailboxController mailboxController;

    public MailboxController getMailboxController() {
        return mailboxController;
    }

    public int getCurrentState()
    {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public String getUid()
    {
        return uid;
    }

    public int getMessageAttribute()
    {
        return messageAttribute;
    }

    public String getReceiveStr()
    {
        return receiveStr;
    }

    public String getSendStr()
    {
        return sendStr;
    }

    public Communication getCommunication() {
        return communication;
    }

    public String getUserNameStr()
    {
        return userNameStr;
    }

    public void increaseMessageAttribute(){ messageAttribute++; }

    public String getFullMessageAttribute() {
        setAtributes();
        return sendStr;
    }

    private void setAtributes()
    {
        sendStr = uid + Hexadecimal_x64.integerToHex32(++messageAttribute);
    }

    public void setReceiveStr(String receiveStr) { this.receiveStr = receiveStr; }

    public void setSendStr(String sendStr){ this.sendStr = sendStr; }

    @FXML
    void connectButtonPressed(ActionEvent event) throws IOException, InterruptedException {
        connect.setDisable(true);
        printMessage("Connecting to " + ipAdress.getText());
        new Thread( ()->{
            try {
                socket = new Socket(ipAdress.getText(), PORT);
                communication = new Communication(socket);
                receiveStr = communication.receive();
                if (responseAnalyzer() == ResponseHandler.OK)
                {
                    connection.setVisible(false);
                    printMessage("Successfully connected");
                    login.setVisible(true);
                    loginButtonThread();
                    uid = receiveStr.substring(0, 2);
                    messageAttribute = 0;
                    currentState = NOT_AUTHENTICATED;
                }
                else throw new ConnectException();
            }
            catch (ConnectException e)
            {
                printMessage("Connection refused. Quiting");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                printMessage("");
                connect.setDisable(false);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loginButtonThread()
    {
        new Thread(() -> {
            boolean loginEnabled = true;
            while (login.isVisible())
            {
                if(userName.getText().isEmpty() || password.getText().isEmpty())
                {
                    if (loginEnabled)
                    {
                        loginButton.setDisable(true);
                        loginEnabled = false;
                    }
                }
                else if (!loginEnabled)
                {
                    loginButton.setDisable(false);
                    loginEnabled = true;
                }
            }
        }
        ).start();
    }

    private void printMessage(String message)
    {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                if (message.isEmpty())
                {
                    status.setText("");
                    return;
                }
                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss // ");
                String formattedTime = myDateObj.format(myFormatObj);
                status.setText(formattedTime + message);
            }
        });
    }

    public void setRoot(Parent root)
    {
        this.root = root;
    }

    @FXML
    private void loginButtonPressed(ActionEvent event) {
        try{
            setAtributes();
            sendStr = sendStr + " LOGIN " + userName.getText() + " " + password.getText();
            communication.send(sendStr);
            receiveStr = communication.receive();
            int response = responseAnalyzer();
        }
        catch (IOException e)
        {
            connectionError();
        }
        if (responseAnalyzer() == ResponseHandler.OK)
        {
            currentState = AUTHENTICATED;
            userNameStr = userName.getText();
            loadMailbox();
            Stage stage = (Stage) connect.getScene().getWindow();
            stage.close();
        }
        else if (responseAnalyzer() == ResponseHandler.NO)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Login is incorrect. Closing connection.", ButtonType.OK);
            alert.showAndWait();
            sendLogout();
            Stage stage = (Stage) connect.getScene().getWindow();
            stage.close();
        }
    }

    private void loadMailbox()
    {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("mailbox.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 590);
            Stage stage = new Stage();
            stage.setTitle("Mailbox");
            mailboxController = (MailboxController)fxmlLoader.getController();
            mailboxController.setValues(this);
            stage.setScene(scene);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Client will disconnect now", ButtonType.OK);
                        alert.showAndWait();
                        event.consume();
                        sendLogout();
                    }
                }
            });
            stage.getIcons().add(new Image("file:src/ICON_client.png"));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int responseAnalyzer()
    {
        if (receiveStr.contains("BYE") && currentState != LOGOUT)
        {
            sendLogout();
        }
        else if (receiveStr.contains("OK"))     return ResponseHandler.OK;
        else if (receiveStr.contains("BAD"))    return ResponseHandler.BAD;
        else if (receiveStr.contains("NO"))     return ResponseHandler.NO;
        return ResponseHandler.UNKNOWN;
    }

    public void sendLogout()
    {
        currentState = LOGOUT;
        setAtributes();
        sendStr = sendStr + " LOGOUT";
        try {
            communication.send(sendStr);
            if (communication.receive().contains("BYE"))
            {
                socket.close();
                connectionClosed();
            }

        } catch (IOException e) {
            connectionError();
        }
    }

    private void connectionClosed()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Connection closed. Exiting.", ButtonType.OK);
        alert.showAndWait();
        System.exit(0);
    }

    private void connectionError()
    {
       Alert alert = new Alert(Alert.AlertType.ERROR,
            "Connection error. Exiting.", ButtonType.OK);
        alert.showAndWait();
        System.exit(0);
    }
}
