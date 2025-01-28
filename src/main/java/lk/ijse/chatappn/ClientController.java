package lk.ijse.chatappn;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientController {

    @FXML
    private TextField msgInput;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField userName;

    private Socket socket;
    private DataOutputStream outputStream;
    private volatile boolean isRunning = true;

    public void initialize() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5001);
                appendMessage("Connected to the server");

                outputStream = new DataOutputStream(socket.getOutputStream());

                new Thread(new MessageReceiver(socket)).start();
            } catch (IOException e) {
                appendMessage("Error connecting to the server: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    void btnDisconnectOnAction(ActionEvent event) {
        try {
            isRunning = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            appendMessage("Disconnected from the server.");
        } catch (IOException e) {
            appendMessage("Error disconnecting: " + e.getMessage());
        }
    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        String message = msgInput.getText().trim();
        if (!message.isEmpty()) {
            try {
                outputStream.writeUTF(userName.getText().trim() + ": " + message);
                outputStream.flush();
                msgInput.clear();
            } catch (IOException e) {
                appendMessage("Error sending message: " + e.getMessage());
            }
        }
    }

    private void appendMessage(String message) {
        Platform.runLater(() -> textArea.appendText(message + "\n"));
    }

    private class MessageReceiver implements Runnable {
        private Socket socket;

        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
                while (isRunning) {
                    String message = inputStream.readUTF();
                    appendMessage(message);

                    if (message.equalsIgnoreCase("stop")) {
                        isRunning = false;
                        socket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    appendMessage("Connection closed.");
                }
            }
        }
    }
}
