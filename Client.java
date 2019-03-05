import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static int port = 12001;
    static String name;
    static Socket client;

    public static void main(String[] args)
    {
        Scanner keyboard = new Scanner(System.in);
        try {
            InetAddress host = InetAddress.getLocalHost();
            client = new Socket(host.getHostAddress(), port);
            //client = new Socket("137.158.58.101", port);

            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            DataInputStream input = new DataInputStream(client.getInputStream());

            logIn(input, output, keyboard);

            System.out.println("Connection has been established, you may start the chat. If you want to send message to some, your message should be the form of [xxx@studentId]");
            System.out.println("Example would be [Hello World@asdfgh002], the studentId is the id of the person you want to send message to.\n");

            Thread receiver = createReceiverThread(input, output);
            Thread sender = createSenderThread(output, keyboard);

            receiver.start();
            sender.start();

        } catch(UnknownHostException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    public static void logIn(DataInputStream input, DataOutputStream output, Scanner keyboard) throws IOException{
        boolean authorization = false;
        while (!authorization) {
            System.out.println("Enter your ID for this app: ");
            String name = keyboard.nextLine();
            System.out.println("Enter your password: ");
            String password = keyboard.nextLine();

            output.writeUTF(name);
            output.writeUTF(password);

            String confirmation = input.readUTF();
            if (confirmation.equals("info-confirm")) {
                authorization = true;
                Client.name = name;
                System.out.println("You have successfully logged in.");
            } else {
                System.out.println("Cannot log in, Please check you input.");
            }
        }
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
                        e.printStackTrace();
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
                                System.out.println("Please enter a valid format of information or command, otherwise the system would not understand what do you want.");
                                System.out.println("If you want to send a message to someone, the format would be [MessageBody@ID] (The id of the person you want to send message to)");
                                System.out.println("Or if you want to type a command, it is usually the format of [verb-noun], available command now are [send-file] and [logout]");
                                info = keyboard.nextLine();
                            }
                        }
                        output.writeUTF(info);
                        output.flush();
                        //Telling the server that the client agreed to accept the file
                        if (info.equals("Y")) {
                            System.out.println("Confirmation sent, please type in the file name: ");
                            String filename = keyboard.nextLine();
                            output.writeUTF(filename);
                        } else if (info.equals("N")) { //Handling when the client refused to accept the file
                            System.out.println("Please type in the name of the person that you refused: ");
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
                System.out.println("File does not exist. Please make sure the file exists and re-enter the filename.");
        }
        System.out.println("Enter the person's  name you want to send to: ");
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
