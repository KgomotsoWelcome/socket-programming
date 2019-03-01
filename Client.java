import java.net.*;
import java.util.*;
import java.io.*;

public class Client{
	
	public static void main(String []args) throws UnknownHostException, IOException {
		
		String machineName = "localhost";
		int portNumber = 2015;
		//DataInputStream input = null;
		//DataOutputStream output = null;
		Socket client = null; 
		Scanner scan = new Scanner(System.in);
		String line;
		
		try{
			client = new Socket(machineName, portNumber);
		}catch(IOException e){
			System.out.println(e);
		}
		
			DataInputStream input = new DataInputStream (client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());

		
		Thread MessageToSend = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
                while (true) { 
  
                    // read the message to deliver. 
                    String message = scan.nextLine(); 
                      
                    try { 
                        // write on the output stream 
                        output.writeUTF(message); 
                    } catch (IOException e) { 
                        e.printStackTrace(); 
                    } 
                } 
            } 
        });
        
        Thread messageToRead = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
  
                while (true) { 
                    try { 
                        // read the message sent to this client 
                        String message = input.readUTF(); 
                        System.out.println(message); 
                    } catch (IOException e) { 
  
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 
        
        MessageToSend.start(); 
        messageToRead.start();
		
	}
}


