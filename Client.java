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
        try
        {
            InetAddress host = InetAddress.getLocalHost();

            client = new Socket(host.getHostAddress(), port);

            PrintStream output = new PrintStream(client.getOutputStream());
            BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("Please enter your name for this app: ");
            name = keyboard.nextLine();
            output.println(name);

            input.readLine();

            Thread receiver = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true)
                    {
                        try
                        {
                            String info = input.readLine();
                            System.out.println(info);
                        }
                        catch(IOException e)
                        {
                            System.out.println(e);
                        }
                    }
                }
            });


            Thread sender = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true)
                    {
                        String info = keyboard.nextLine();
                        output.println(info);
                    }
                }
            });

            receiver.start();
            sender.start();


        }
        catch(UnknownHostException e)
        {
            System.out.println(e);
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
    }

}
