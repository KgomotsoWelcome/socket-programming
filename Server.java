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
    
     /**
     * An ArrayList of a type of thread that handles the client connection ;
     * to the server; defined by a nested class.
     */
    static ArrayList<ClientHandler> handler = new ArrayList<>();
    
    /**
     * An ArrayList that holds the information about the client, such 
     * as the name and password; defined by a nested class.
     */
    static ArrayList<ClientInfo> clientList = new ArrayList<>();

	/**
     * Beginning of main method.
     */
    public static void main(String[] args)
    {
		System.out.println("*******************************Server*********************************");
		System.out.println();
		
		/**
         * Listens for a connection request on a specified port. Accepts connection requests
         * from client socket. Creates input and ouput streams for data transfer.
         * Creates thread for each client that was accepted by the server socket.
         * Adds client to handler array.
         */
        ServerSocket server = null;
        try {
            loadClientList();
            server = new ServerSocket(port);

            while(true) {
                System.out.println("Listening for request from clients...");
                Socket client = server.accept();
                System.out.println("Received request from clients, connection established.");

                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());

                ClientHandler ch = new ClientHandler(client, input, output);
                handler.add(ch);
                System.out.println("New client has been added to the client list.");
                ch.start();
		System.out.println();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * End of main method of server class.
     */


	/**
	 * Verifies the username and the password of the user.
	 * ID and password should match for the user to login. 
     */
    static String logInCheck(DataInputStream input, DataOutputStream output) throws IOException {
	System.out.println();
        while (true) {
            String name = input.readUTF(); //Read in the client ID
            String password = input.readUTF();

            for (ClientInfo info : clientList) {
                if (info.checkExist(name) && info.checkPassword(password)) {
                    output.writeUTF("info-confirm");
                    System.out.println("Client " + name + " has logged in.");
                    return name;
                }
            }
            output.writeUTF("login-fail");
        }
    }
    
    /**
	 * Loads the client list from a file into a data structure.
	 * Improves the efficiency of the program. 
     */
    static void loadClientList() throws IOException {
        File listFile = new File("ClientList.txt");
        BufferedReader input = new BufferedReader(new FileReader(listFile));
        String info;
        while ((info =input.readLine()) != null) {
            StringTokenizer token = new StringTokenizer(info);
            ClientInfo tempClient = new ClientInfo(token.nextToken(), token.nextToken());
            clientList.add(tempClient);
        }
        input.close();
    }
}

	/**
     * Defines the thread that handles the connection. The thread is responsible
     * for opening the connection and for receiving messages.  This class contains
     * several methods that are called by the main class, and that are therefore
     * executed in a different thread. The thread handles the file transmission used
     * throughout the program.
     **/
     
class ClientHandler extends Thread {
    private Socket client;
    private final DataInputStream input;
    private final DataOutputStream output;
    private boolean isOnline = false;
    private String name;

	//The constructor sets some instance variables.
    public ClientHandler(Socket client, DataInputStream input, DataOutputStream output) {
        this.client = client;
        this.input = input;
        this.output = output;
    }

	//Overrides run method from the Thread class
	@Override
    public void run()
    {
        try {
            name = Server.logInCheck(input, output);
            isOnline = true;
        }catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                String message = input.readUTF();

                if (message.equals("logout")) {
                    output.writeUTF("logout");
                    isOnline = false;
                    break; //It will break out of this loop and go to the try-catch block
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
                            ch.output.writeUTF("receive-file-confirmation");
                            ch.output.writeUTF(filename);
                            ch.output.writeUTF(name);
                            output.flush();
                        }
                    }

                }
                //Handling for file transmission, Y stands for agree to accept the file, vice versa
                else if (message.equals("Y")) {
                    String filename = input.readUTF();
                    output.writeUTF("receive-file");
                    sendFile(output, filename);
                }
                else if (message.equals("N")) { //Handling for client that refused to accept the file
                    String fileSender = input.readUTF();
                    for (ClientHandler ch : Server.handler) {
                        if (ch.name.equals(fileSender)) {
                            ch.output.writeUTF(name + " refused to receive your file.");
                            output.flush();
                        }
                    }
                }
                else {
                    //Trying to get the client name that the message should goes to
                    StringTokenizer messageHandler = new StringTokenizer(message, "@");
                    String messageBody = messageHandler.nextToken();
                    String recipient = messageHandler.nextToken();
                    messageBody = name + ": " + messageBody;
                    boolean online = false;

                    //Looping through all the clients in the ArrayList to find the client that match the name
                    for (ClientHandler ch : Server.handler) {
                        if (ch.isOnline && ch.name.equals(recipient)) { //isOnline makes sure it is a logged in client instead of client that are still trying to log in
                            ch.output.writeUTF(messageBody);
                            online = true;
                            //break;
                        }
                    }

                    if (!online)
                        output.writeUTF(" Server: User " + recipient + " is not online.");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        //Try to close all the stream and remove itself from the handler ArrayList
        try {
            input.close();
            output.close();
            client.close();
            System.out.println("Client " + name + " has gone offline");
            Server.handler.remove(this);
            return; //To terminate the thread
        } catch(IOException e) {
            System.out.println(e);
        }
    }

	//Takes in the filename and saves the file to the directory
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

        fos.close(); // Close the file output stream
    }

	//
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
	/**
     * End of inner handler class.
     */


/**
 * Beginning of inner class containing accessor 
 * and mutator methods to have acces to clients 
 * ID and password. 
 **/
class ClientInfo {
    private String id;
    private String password;
    public ClientInfo(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public boolean checkExist(String name) {
        return id.equals(name);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
