package lk.ijse.chatappn;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int PORT = 5001;
    private static Set<Socket> clientSockets = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastMessage(String message, Socket senderSocket) {
        for (Socket socket : clientSockets) {
            if (socket != senderSocket) {
                try {
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF(message);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())) {
                String message;

                while ((message = inputStream.readUTF()) != null) {
                    System.out.println("Message received: " + message);

                    if (message.equalsIgnoreCase("stop")) {
                        clientSockets.remove(clientSocket);
                        clientSocket.close();
                        System.out.println("Client disconnected");
                        break;
                    }

                    broadcastMessage(message, clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
