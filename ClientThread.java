import java.net.*;
import java.util.*;
import java.io.*;

public class ClientThread implements Runnable{
	
	Socket client = null;
	private String clientName;
	DataInputStream input;
	DataOutputStream output;
	Scanner scan;
	Server server; 
	boolean isloggedin;
	
	
	public ClientThread(Socket client, String clientName, 
                            DataInputStream input, DataOutputStream output){
		this.client = client;
		this.clientName = clientName;
		this.input = input;
		this.output = output;
		this.isloggedin = true;
	}
	
	public void run(){
		
		String received;
			
			while(true){
				
					try{
						received = input.readUTF();
						
						System.out.println(received);
						
						if(received.equals("logout")){ 
							this.isloggedin=false; 
							this.client.close(); 
							break; 
						}
						
						StringTokenizer st = new StringTokenizer(received, "#"); 
						String MsgToSend = st.nextToken(); 
						String recipient = st.nextToken(); 
						
						for (ClientThread i :server.clients)  
						{ 
							// if the recipient is found, write on its 
							// output stream 
							if (i.clientName.equals(recipient) && i.isloggedin==true)  
							{ 
								i.output.writeUTF(this.clientName+" : "+MsgToSend); 
								break; 
							} 
						} 
						
					}catch(IOException e){
						System.out.println(e);
					}
				}
				try
				{ 
					// closing resources 
					this.input.close(); 
					this.output.close(); 
					  
				}catch(IOException e){ 
					e.printStackTrace(); 
				}
				/**
				
				String text = input.readUTF();
				messageToAllClients(text);
			}
			input.close();
			output.close();
			client.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	public void messageToClient(String message){
		try{
			output.writeUTF(message);
			output.flush();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	public void messageToAllClients(String message){
		for (int i = 0; i< server.clients.size();i++){
			ClientThread c = server.clients.get(i);
			c.messageToClient(message);
		}
	}**/
	
}
}
