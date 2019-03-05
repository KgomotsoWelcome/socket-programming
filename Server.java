import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server
{
	/**
     * Default port number.  This is the initial content of input boxes in
     * the window that specify the port number for the connection. 
     */
    private static int port = 12001;
    static Client clientClass = new Client();
    static ArrayList<ClientHandler> handler = new ArrayList<ClientHandler>();
    
	
	
    public static void main(String[] args)
    {
		
		System.out.println("*******************************Server*********************************");
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
			
            while(true) {
				
				System.out.println();
                System.out.println("Waiting for request from clients...");
                Socket client = server.accept();
                System.out.println("Received request from clients, connection established.");

                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());

                String name = input.readUTF(); //Read in the client online name
                output.writeUTF("name-confirmed"); //Telling the client that the online name has been received

                ClientHandler ch = new ClientHandler(client, new DataInputStream(client.getInputStream()), output, name);
                handler.add(ch);
                
                
                System.out.println(name+" has been added to the client list!");
                ch.start();
            }

        } catch(IOException e) {
            System.out.println("Sorry, could not open Data Streams.");
        }
    }
}

class ClientHandler extends Thread {
    private Socket client;
    private final DataInputStream input;
    private final DataOutputStream output;
    private boolean isOnline = true;
    public final String name;

    public ClientHandler(Socket client, DataInputStream input, DataOutputStream output, String name) {
        this.client = client;
        this.input = input;
        this.output = output;
        this.name = name;
    }

    public void run()
    {
		/**Prints out the names of the people that are online
			 * to allow the user to choose who to communicate with
			 **/
		try{	
			output.writeUTF("The following people are online");
			for (ClientHandler clients : Server.handler) {
				output.writeUTF(clients.name);
			}
			output.writeUTF("Choose the person you would like to talk to.");
		}	
		
		catch (IOException e)
			{System.out.println("Could not print the list of names");}	
			
        while(true) {
			    
            try {
                String message = input.readUTF();

                if (message.equals("logout")) {
                    isOnline = false;
                    break;
                }

                if (message.equals("send-file")) {
                    //Receive and save the file from source client
                    output.writeUTF("send-file-confirmed");
                    String filename = "server" + input.readUTF();
                    String recipient = input.readUTF();
                    saveFile(filename);

                    //Find and send the file to the recipient
                    for (ClientHandler ch : Server.handler) {
                        if (ch.name.equals(recipient)) {
                            ch.output.writeUTF("receive-file");
                            output.flush();
                            sendFile(ch.output, filename);
                        }
                    }
                }
                else {
                    //Trying to get the client name that the message should goes to
                    StringTokenizer messageHandler = new StringTokenizer(message, "@");
                    String message1 = messageHandler.nextToken();
                    String recipient = messageHandler.nextToken();
                    
                    //Looping through all the clients in the ArrayList to find the client that match the name
                    for (ClientHandler ch : Server.handler) {
                        if (ch.name.equals(recipient))
                            ch.output.writeUTF(message1);
                    }
                }
            } catch(IOException e) {
                System.out.println("The client has disconnected.");
                break;
            }
        }

        //Try to close all the stream
        try {
            client.close();
            input.close();
            output.close();
        } catch(IOException e) {
            System.out.println("All the open streams have been closed.");
        }
    }

    void saveFile(String filename) throws IOException
    {
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
    }

    void sendFile(DataOutputStream output, String filename) throws IOException
    {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[8192];
        long length = file.length();

        output.writeUTF(filename);
        output.writeLong(length);
        output.flush();

        int count;
        while ((count = fis.read(buffer)) > 0)
            output.write(buffer, 0, count);

        System.out.println("File has been sent.");
        fis.close();
    }

}
