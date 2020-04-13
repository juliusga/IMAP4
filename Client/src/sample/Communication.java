package sample;

import java.io.*;
import java.net.Socket;

public class Communication {
    private Socket socket;
    Communication (Socket socket)
    {
        this.socket = socket;
    }

    public void send(String message) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
        System.out.println("[SENT] " + message);
    }

    public String receive() throws IOException {
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        String message = dataInputStream.readUTF();
        System.out.println("[RECEIVE] " + message);
        return message;
    }
}
