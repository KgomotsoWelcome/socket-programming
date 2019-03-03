import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ChatGUI extends JFrame{

  JButton send;
  JButton sendFile;
  JPanel messagepanel;
  JTextField type_message;
  JLabel type_message1;
  JTextField text;

public static void main (String args[]){
   new ChatGUI();
}
  public ChatGUI(){
  
    super("ChatApp");

    send = new JButton("Send message");
    sendFile = new JButton("Send file");
    messagepanel = new JPanel();
    type_message = new JTextField(15);
    type_message1 = new JLabel("Type your message. ");
    text = new JTextField();

    setSize(600,600);
    setLocation(400,600);
    messagepanel.setLayout (null); 

    type_message.setBounds(150,30,250,50);
    send.setBounds(450,45,100,20);
    type_message1.setBounds(10,10,500,100);
    sendFile.setBounds(90,100,400,35);
    text.setBounds(50,150,500,400);

    messagepanel.add(send);
    messagepanel.add(type_message);
    messagepanel.add(type_message1);
    messagepanel.add(sendFile);
    messagepanel.add(text);

    getContentPane().add(messagepanel);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);  
    
    type_message.addActionListener(new ActionListener() {
    
      @Override
      public void actionPerformed(ActionEvent e) {
         //place method to take in message from client
      }
    });
    
    
    text.addActionListener(new ActionListener() {
    
      @Override
      public void actionPerformed(ActionEvent e) {
         //display string of messages
      }
    });
    
 
    send.addActionListener(new ActionListener() {
    
      @Override
      public void actionPerformed(ActionEvent e) {
         //send message 
      }
    });

    }

}