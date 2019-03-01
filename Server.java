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
        try
        {
            server = new ServerSocket(port);

            while(true)
            {
                Socket client;
                System.out.println("Waiting for request from clients...");
                client = server.accept();

                System.out.println("Received request from clients, connection established.");

                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

                PrintStream output = new PrintStream(client.getOutputStream());
                String name = input.readLine();
                output.println("Name received.");

                ClientHandler ch = new ClientHandler(client, input, output, name);

                handler.add(ch);
                System.out.println("New client has been added to the client list.");
                ch.start();

            }


        }
        catch(IOException e)
        {
            System.out.println(e);
        }


    }

}

class ClientHandler extends Thread {
    Socket client;
    final BufferedReader input;
    final PrintStream output;
    boolean isOnline = true;
    final String name;

    public ClientHandler(Socket client, BufferedReader input, PrintStream output, String name) {
        this.client = client;
        this.input = input;
        this.output = output;
        this.name = name;
    }

    public void run()
    {
        while(true)
        {
            try
            {
                String message = input.readLine();

                if (message.equals("logout"))
                {
                    isOnline = false;
                    break;
                }
                StringTokenizer messageHandler = new StringTokenizer(message, "@");
                String messageBody = messageHandler.nextToken();
                String recipient = messageHandler.nextToken();


                for (ClientHandler ch : Server.handler )
                {
                    if (ch.name.equals(recipient))
                        ch.output.println(message);
                }
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }

        try
        {
            client.close();
            input.close();
            output.close();
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
    }
}
