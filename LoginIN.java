import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class LoginIN extends JFrame{
  JButton login;
  JPanel loginpanel;
  JTextField username;
  JTextField password;
  JLabel username1;
  JLabel password1;

public static void main (String args[]){
   new LoginIN();
}
  public LoginIN(){
    super("Welcome to the chatApp");

    login = new JButton("Login");
    loginpanel = new JPanel();
    username = new JTextField(15);
    password = new JPasswordField(15);
    username1 = new JLabel("Username");
    password1 = new JLabel("Password");

    setSize(300,200);
    setLocation(500,280);
    loginpanel.setLayout (null); 

    username.setBounds(70,30,150,20);
    password.setBounds(70,65,150,20);
    login.setBounds(110,100,80,20);
    username1.setBounds(5,28,80,20);
    password1.setBounds(5,63,80,20);

    loginpanel.add(login);
    loginpanel.add(username);
    loginpanel.add(password);
    loginpanel.add(username1);
    loginpanel.add(password1);

    getContentPane().add(loginpanel);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);
   
    login.addActionListener(new ActionListener() {
    
      @Override
      public void actionPerformed(ActionEvent e) {
      
             String user_name = username.getText();
             String user_password = password.getText();
  
             if(user_name.equals("wlckgo001") && user_password.equals("wlckgo001")) {
               ChatGUI chat = new ChatGUI();
               dispose();
             } 
             else if(user_name.equals("chnleo007") && user_password.equals("chnleo007")){
               ChatGUI chat = new ChatGUI();
               dispose();
   
             }
             else if(user_name.equals("xkzmus001") && user_password.equals("xkzmus001")){
               ChatGUI chat = new ChatGUI();
               dispose();
   
             }
             else {
               JOptionPane.showMessageDialog(null,"The Username or Password you entered is incorrect. Please try again.");
             }
      }
    });
    
    }
}