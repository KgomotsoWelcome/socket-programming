import java.net.*;
import java.io.*;
import java.util.*;

public class Server{
	
	static ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	public static void main (String []args){
		
		int portNumber = 2015;
		ServerSocket service = null;
		Socket serviceSocket = null;
		
		
		int clientCounter = 0; 

		
		DataInputStream input;
		DataOutputStream output;
		String result;
			
		try{
		 service = new ServerSocket(portNumber);
		 serviceSocket = new Socket();
		
		}catch(IOException e){
			System.out.println(e);
		}
			
		while(true){
			try{
				
				System.out.println("Client is requesting to connect.....");
				serviceSocket = service.accept();
				System.out.println("Client request is accepted");
				
				input = new DataInputStream (serviceSocket.getInputStream());
				output = new DataOutputStream(serviceSocket.getOutputStream());
				
				ClientThread connectedClients = new ClientThread(serviceSocket,"client " + clientCounter, input, output);
				Thread thread = new Thread(connectedClients);
				System.out.println("Adding this client to active client list");
				clients.add(connectedClients);
				thread.start();
				
				
				clientCounter++;
			}
				//result = input.readLine();
				//System.out.println("Message: "+result);
				//input.close();
				//output.close();
				//serviceSocket.close();
				//service.close();
				
			catch(IOException ex){
					System.out.println(ex);
			}
		}
		}
}




