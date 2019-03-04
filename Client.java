import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static int port = 2222;
    static String name;

    public static void main(String[] args)
    {
        Socket client;
        Scanner keyboard = new Scanner(System.in);
        try {
            InetAddress host = InetAddress.getLocalHost();
            client = new Socket(host.getHostAddress(), port);

            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            DataInputStream input = new DataInputStream(client.getInputStream());

            System.out.println("Please enter your name for this app: ");
            name = keyboard.nextLine();

            boolean nameReceived = false; //To check whether the server has received the name
            while (!nameReceived) {
                output.writeUTF(name); //Telling the server what is the online name of the client
                if (input.readUTF().equals("name-confirmed")) //Confirmation from the server that the client name has been received
                    nameReceived = true;
            }

            System.out.println("Connection has been established, you may start the chat.\n");

            Thread receiver = createReceiveThread(input);
            Thread sender = createSendThread(output, keyboard);

            receiver.start();
            sender.start();

        } catch(UnknownHostException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static Thread createReceiveThread(DataInputStream input)
    {
        Thread receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        String info = input.readUTF();
                        System.out.println(info);
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
                while(true)
                {
                    try {
                        String info = keyboard.nextLine();
                        output.writeUTF(info);
                        output.flush();
                        if (info.equals("send-file")) {
                            sendFile(output, keyboard);
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


    synchronized static void sendFile(DataOutputStream output, Scanner keyboard) throws IOException
    {
        System.out.println("Enter the file name you want to transfer: ");
        String filename = keyboard.nextLine();
        System.out.println("Enter the person's  name you want to send to: ");
        String name = keyboard.nextLine(); //You will have to remove these two lines

        //Open the file and read from it, use File to get the size of the file, and use FileInputStream to read the data of the file
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[8192]; //Create a buffer for the data to be transported
        long length = file.length();

        output.writeUTF(filename);
        output.writeLong(length);
        output.flush(); //To make sure the stream is clean

        int count; //To count how many bytes have been read every time
        while ((count = fis.read(buffer)) > 0)
            output.write(buffer, 0, count);

        fis.close();
    }

    public void receiveFile()
    {

    }

}
