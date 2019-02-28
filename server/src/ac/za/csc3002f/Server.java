package ac.za.csc3002f;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        handleNewClients();
    }

    private void handleNewClients() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(clientSocket);
                workerList.add(worker);
                worker.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
