import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.Console;


public class Client {
	 static ArrayList<String> client_names = new ArrayList<String>();

	/**
     * Default port number.  This is the initial content of input boxes in
     * the window that specify the port number for the connection. 
     */
    private static int port = 12001;
    
    /**
     * The name and password variable stores the name and the password
     * of the user. This is later used to check the authentification of the user 
     * before the client logs into the system.
     **/
    static String name;
    static String password;
    static Console console = System.console();
    
     /**
     * The name of the recipient is stored and later used to help the server
     * know which destination to send the message to.
     **/
    static String client_recipient;
    static Server server_class = new Server();
    
    /**
     * This method implementation is used to clear the screen
     * to keep the display of the application nice and neat
     **/
    public static void clearScreen() {  
		System.out.print("\033[H\033[2J");  
		System.out.flush();  
	} 
    
    public static void main(String[] args)
    {
        Socket client;
        Scanner keyboard = new Scanner(System.in);
        try {
            InetAddress host = InetAddress.getLocalHost();
            client = new Socket(host.getHostAddress(), port);

            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            DataInputStream input = new DataInputStream(client.getInputStream());
			 /**
			 * An added feature to check the authentification 
			 * of the user before accessing the ChatApp. The user 
			 * to use specific login details to access the ChatApp.
			 **/
			System.out.println("***************************Welcome to the ChatApp**********************");
            
            while(true){
				
				System.out.print("Please enter your name: ");
				System.out.println();
				name = keyboard.nextLine();
            
				//System.out.println("Please enter your password: ");
				char[] pw = console.readPassword("Please enter your password: ");
				
				//password = keyboard.nextLine();
			
				if((name.equals("wlckgo001"))||(name.equals("chnleo007"))||(name.equals("xkzmus001")))
				{
					System.out.println("You have been successfully connected!");
					client_names.add(name);
					break;
				}else{
					System.out.println("The Username or Password you entered is incorrect. Please try again.");
				}
			}
			
			clearScreen(); // clears the screen
            boolean nameReceived = false; //To check whether the server has received the name
            while (!nameReceived) {
                output.writeUTF(name); //Telling the server what is the online name of the client
                if (input.readUTF().equals("name-confirmed")) //Confirmation from the server that the client name has been received
                    nameReceived = true;
            }

            Thread receiver = createReceiveThread(input);
            Thread sender = createSendThread(output, keyboard);
		
            receiver.start();
            sender.start();

        } catch(UnknownHostException e) {
            System.out.println("Unknown host");
        } catch(IOException e) {
            System.out.println("The server is disconnected");;
        }
    }
    public static Thread createReceiveThread(DataInputStream input)
    {
        Thread receiver = new Thread(new Runnable() {
            @Override
            public void run() {
				//System.out.println("Choose the person you would like to talk to.");
                while(true)
                {
                    try {
                        String info = input.readUTF();
                        System.out.println(info);
                        if (info.equals("receive-file")) {
                                saveFile(input);
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        return receiver;
    }

    public static Thread createSendThread(DataOutputStream output, Scanner keyboard)
    {
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
				clearScreen();
				client_recipient = keyboard.nextLine();	
				System.out.println(name+ " --------> "+ client_recipient);
				System.out.println("Type the message you want to send or type \"send-file\" to send a file.");
				
                while(true)
                {
                    try {
                        String info = keyboard.nextLine();
                        output.writeUTF(name +" : " +info);
                        output.flush();
                        if (info.equals("send-file")) {
                            sendFile(output, keyboard);
                            System.out.println("File has been sent.");
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        return sender;
    }

    static void sendFile(DataOutputStream output, Scanner keyboard) throws IOException
    {
        System.out.println("Type the name of the file: ");
        String filename = keyboard.nextLine();

        //Open the file and read from it, use File to get the size of the file, and use FileInputStream to read the data of the file
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        

        byte[] buffer = new byte[8192]; //Create a buffer for the data to be transported
        long length = file.length();

        output.writeUTF(filename);
        output.writeUTF(client_recipient);
        output.writeLong(length);
        output.flush(); //To make sure the stream is clean

        int count; //To count how many bytes have been read every time
        while ((count = fis.read(buffer)) > 0)
            output.write(buffer, 0, count);

        fis.close();
    }

    static void saveFile(DataInputStream input) throws IOException
    {
        String filename = name + input.readUTF();
        long length = input.readLong();
        int read;
        long remaining = length;
        byte[] buffer = new byte[8192];
		
        FileOutputStream fos = new FileOutputStream(filename);

        while((read = input.read(buffer, 0, Math.min(buffer.length, (int)remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }

        System.out.println(filename+" has been saved.");
        fos.close();

    }

}
