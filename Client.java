import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.Console;

public class Client {

	/**
     * Default port number.  This is the initial content of input boxes in
     * the window that specify the port number for the connection. 
     */
    private static int port = 12001;
    
    /**
     * Stores the name of the client
     */
    static String name;
    
    /**
     * Socket connects to IP address
     * and port number.
     */
    static Socket client;

	/**
     * Beginning of main method.
     */
    public static void main(String[] args)
    {
		// Scanner object that will be used to take in input from the user
        Scanner keyboard = new Scanner(System.in);
        
        /**
         * Connects socket using IP address and default port number.
         * Opens input/output streams to transport data from client 
         * to server and vice versa. Throws UnknownHostException and 
         * IOException. Starts the threads used for communication.
         **/
        try {
            InetAddress host = InetAddress.getLocalHost();
            client = new Socket(host.getHostAddress(), port);

            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            DataInputStream input = new DataInputStream(client.getInputStream());

            logIn(input, output, keyboard);
            
            System.out.println("0. To send a message use the format: [MessageBody@name] e.g \"hello@wlckgo001\" (use the name of the person you are sending the message to.)");
			System.out.println("1. To send a file type the command \"send-file\" ");
			System.out.println("2. To logout, type \"logout\" ");

            Thread receiver = createReceiverThread(input, output);
            Thread sender = createSenderThread(output, keyboard);

            receiver.start();
            sender.start();

        } catch(UnknownHostException e) {
            System.out.println("Unknown host");
        } catch(IOException e) {
            System.out.println("The server is disconnected");
        }
    }
    
    /**
     * This method implementation is used to clear the screen
     * to keep the display of the application nice and neat
     **/
    
	public static void clearScreen() {  
		System.out.print("\033[H\033[2J");  
		System.out.flush();  
	} 

	/**
	 * An added feature to check the authentification 
	 * of the user before accessing the ChatApp. The user 
	 * to use specific login details to access the ChatApp.
	 **/
    public static void logIn(DataInputStream input, DataOutputStream output, Scanner keyboard) throws IOException{
        System.out.println("***************************Welcome to the ChatApp**********************");
        boolean authorization = false;
        while (!authorization) {
            System.out.print("Please enter your name: ");
            System.out.println();
            String name = keyboard.nextLine();
            Console console = System.console();
            char[] pw = console.readPassword("Please enter your password: ");
            String password = String.valueOf(pw);

            output.writeUTF(name);
            output.writeUTF(password);

            String confirmation = input.readUTF();
            if (confirmation.equals("info-confirm")) {
                authorization = true;
                Client.name = name;
                System.out.println("You have successfully logged in.");
            } else {
                System.out.println("The username or password you entered is incorrect. Please try again.");
            }
        }
        clearScreen();
    }

    public static Thread createReceiverThread(DataInputStream input, DataOutputStream output)
    {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        String info = input.readUTF();
                        if (info.equals("receive-file-confirmation")) {
                            String filename = input.readUTF();
                            String name = input.readUTF();
                            System.out.println("Do you want to accept file [" + filename + "] from " + name + "? Type 'Y' or 'N'");
                        } else if (info.equals("receive-file"))
                            saveFile(input);
                        else if (info.equals("logout")) {
                            input.close();
                            output.close();
                            client.close();
                            System.exit(666);
                        }
                        else
                            System.out.println(info);
                    } catch (IOException e)
                    {
                        System.out.println("Could not read the input stream.");
                        break;
                    }
                }
            }
        });
    }

    public static Thread createSenderThread(DataOutputStream output, Scanner keyboard)
    {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        String info = keyboard.nextLine();
                        boolean valid = false;
                        while (!valid) { //To check whether the input line is valid
                            if (info.contains("@") || info.equals("Y") || info.equals("N") || info.equals("send-file") ||info.equals("logout"))
                                valid = true;
                            else {
                                System.out.println("Error. Please enter valid command. ");
								System.out.println("0. To send a message use the format: [MessageBody@name] e.g \"hello@wlckgo001\" ");
								System.out.println("1. To send a file type the command \"send-file\" ");
                                //System.out.println("Or if you want to type a command, it is usually the format of [verb-noun], available command now are [send-file] and [logout]");
                                info = keyboard.nextLine();
                            }
                        }
                        output.writeUTF(info);
                        output.flush();
                        //Telling the server that the client agreed to accept the file
                        if (info.equals("Y")) {
                            System.out.println("Please type the file name: ");
                            String filename = keyboard.nextLine();
                            output.writeUTF(filename);
                        } else if (info.equals("N")) { //Handling when the client refused to accept the file
                            System.out.println("Please type the name of the person who requested to send you the file: ");
                            String name = keyboard.nextLine();
                            output.writeUTF(name);
                        } else if (info.equals("send-file")) {//Telling the server that the client wants to send a file to another client
                                sendFile(output, keyboard);
                                System.out.println("File has been sent to the server.");
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    static void sendFile(DataOutputStream output, Scanner keyboard) throws IOException
    {
        boolean fileExists = false;
        String filename = null;
        File file = null;
        while (!fileExists) {
            System.out.println("Enter the file name you want to transfer: ");
            filename = keyboard.nextLine();
            file = new File(filename);
            if (file.exists())
                fileExists = true;
            else
                System.out.println("File does not exist. Try again.");
        }
        System.out.println("Enter the name of the receiver: ");
        String name = keyboard.nextLine(); //You will have to remove these two lines

        //Open the file and read from it, use File to get the size of the file, and use FileInputStream to read the data of the file
        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[8192]; //Create a buffer for the data to be transported
        long length = file.length();

        output.writeUTF(filename);
        output.writeUTF(name);
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

        System.out.println("File has been saved.");
        fos.close();

    }

}
