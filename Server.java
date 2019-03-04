import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server
{

    private static int port = 2222;
    static ArrayList<ClientHandler> handler = new ArrayList<ClientHandler>();

    public static void main(String[] args)
    {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);

            while(true) {
                System.out.println("Waiting for request from clients...");
                Socket client = server.accept();
                System.out.println("Received request from clients, connection established.");

                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());

                String name = input.readUTF(); //Read in the client online name
                output.writeUTF("name-confirmed"); //Telling the client that the online name has been received

                ClientHandler ch = new ClientHandler(client, new DataInputStream(client.getInputStream()), output, name);
                handler.add(ch);
                System.out.println("New client has been added to the client list.");
                ch.start();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }


    }

}

class ClientHandler extends Thread {
    private Socket client;
    private final DataInputStream input;
    private final DataOutputStream output;
    private boolean isOnline = true;
    private final String name;

    public ClientHandler(Socket client, DataInputStream input, DataOutputStream output, String name) {
        this.client = client;
        this.input = input;
        this.output = output;
        this.name = name;
    }

    public void run()
    {
        while(true) {
            try {
                String message = input.readUTF();

                if (message.equals("logout")) {
                    isOnline = false;
                    break;
                }

                if (message.equals("send-file")) {
                    output.writeUTF("send-file-confirmed");
                    output.flush();
                    saveFile();
                }
                else {
                    //Trying to get the client name that the message should goes to
                    StringTokenizer messageHandler = new StringTokenizer(message, "@");
                    messageHandler.nextToken();
                    String recipient = messageHandler.nextToken();

                    //Looping through all the clients in the ArrayList to find the client that match the name
                    for (ClientHandler ch : Server.handler) {
                        if (ch.name.equals(recipient))
                            ch.output.writeUTF(message);
                    }
                }
            } catch(IOException e) {
                System.out.println(e);
            }
        }

        //Try to close all the stream
        try {
            client.close();
            input.close();
            output.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    synchronized void saveFile() throws IOException
    {
        String filename = "client" + input.readUTF();
        long length = input.readLong(); //Get the size of the file
        int read = 0; //To show how many bytes have been read each time
        long remaining = length; //Remaining bytes to be read
        byte[] buffer = new byte[8192];

        FileOutputStream fos = new FileOutputStream(filename);
        while ((read = input.read(buffer, 0, Math.min(buffer.length, (int)remaining))) > 0)  {
            remaining -= read;
            fos.write(buffer, 0, read);
        }

        System.out.println("File has been saved.");

        fos.close();
        input.close();
    }

    public void sendFile() throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream("send.mp4");
        byte[] buffer = new byte[8192];
        int count =0;

        while ((count = fis.read(buffer)) > 0)
            output.write(buffer);

        fis.close();
    }

}
