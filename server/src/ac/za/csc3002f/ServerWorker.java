package ac.za.csc3002f;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;


public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String username;

    public ServerWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.outputStream = clientSocket.getOutputStream();
            this.inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket( ) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);

            if (tokens != null && tokens.length > 0){
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd)) {
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }
                else {
                    String msg = "unknown: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String username = tokens[1];
            String password = tokens[2];

            if ((username.equals("xkzmus001") && password.equals("xkzmus001")) || (username.equals("guest") && username.equals("guest") )) {
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.username = username;
                System.out.println("User logged in successfully: " + username);
            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }
}
