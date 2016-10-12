/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Dragon
 */
public class TCPServer {
    //All the clients connected
    public static Vector ConnectedUsers = new Vector (); 
   
    //Clients info
    public static int ClientNum = 0;
    public static String ClientUsername = "";
    
    // 4 usernames and password for authentication
    public static String Username[] = {"tester","alpha","hunt","beta"};
    public static String Password[] = {"password","password","password","password"};
    
    public static boolean ServerOn = true;
    
    // TCP Components 
    public static ServerSocket myServerSocket;
    public static Socket clientSocket;
    public static BufferedReader recieve = null;
    public static PrintWriter send = null;
   
    
    public TCPServer(){       
      try {
          
        myServerSocket = new ServerSocket(1234);
        while (ServerOn)
        {
           clientSocket = myServerSocket.accept();
           //Server has accepted client for authentication
           Authentication();
        }
      }
      catch(Exception e) {
        System.out.println("Whoops! Create server and Accept client didn't work!");
      }
    }

    public static void main(String args[]) { 
        new TCPServer();
    }
    
    /**
     *  Clients establish to the server and sends login info for verification
     */
    public void Authentication(){
        
        
        try{
            recieve = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
            send = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
            // Recieves login info for verification
            String authenticate = recieve.readLine();
            
            for(int i=0;i<Username.length;i++){
                if(authenticate.equals(Username[i] + Password[i])){
                    // Incremment ClientNum to know how many are connected
                    ClientNum++;
                    ClientUsername = Username[i];
                    // Adds Clients username to ConnectedUsers
                    ConnectedUsers.addElement(Username[i]);
                    
                    // Sends info to client that Username and Password is correct
                    send.println("Authentication Successful!");
                    send.flush();
                    
                    // Sends clients num to client to know ClientID
                    send.println(ClientNum);
                    send.flush();
                    
                    // Starts a thread for client to communicate with server
                    // This enables server to continue listening for new clients
                    ClientServiceThread ClientsThread = new ClientServiceThread(clientSocket);
                    ClientsThread.start();
                    break;                
                   }
                // When Username and password is incorrect
                else if(i == Username.length-1){
                    // Sends Authentication Unsuccessful to clients
                    send.println("Authentication Unsuccessful!");
                    send.flush();    
                }

            }
        }
        catch(Exception e) {
            System.out.print("Whoops! It didn't work!\n");
      }
        
    }
    /*
    * Starts a thread for client to communicate with server
    * This enables server to continue listening for new clients
    */
    class ClientServiceThread extends Thread 
    { 
        String myUsername = ClientUsername;
        Socket myClientSocket;
        boolean ClientRunThread = true; 

        public ClientServiceThread() 
        { 
            super(); 
        } 

        ClientServiceThread(Socket s) 
        { 
            myClientSocket = s;
        } 

        public void run() 
        {    
            BufferedReader in = null; 
            PrintWriter out = null; 
            
            // Print out details of this connection 
            System.out.println("Accepted Client Address: " + myClientSocket.getInetAddress().getHostName()); 

            try 
            {   
                // Command which will be received from client
                String Command;
                
                in = new BufferedReader(new InputStreamReader(myClientSocket.getInputStream())); 
                out = new PrintWriter(new OutputStreamWriter(myClientSocket.getOutputStream())); 
 
                // Loop until ClientRunThread is set to false 
                while(ClientRunThread) 
                {   
                    //recieve the command from client
                    Command = in.readLine();
                    if(!ServerOn) 
                    { 
                        System.out.print("Server has already stopped");
                        ClientRunThread = false;
                    }
                    switch (Command) {
                        case "LOGOUT":
                            // Quit this thread
                            ClientRunThread = false;
                            System.out.print("Stopping client thread for client : ");
                            break;
                        case "UPDATE_ONLINE_USERS":
                            // Sends the total clients connected to server
                            out.println(ClientNum);
                            out.flush();
                            // Send the details of online users
                            for(int i=0;i<ConnectedUsers.size();i++)
                            {
                                out.println(ConnectedUsers.elementAt(i));
                                out.flush();
                            }   break;
                        default:
                            break;
                    }
                } 
            } 
            catch(Exception e) 
            { 
                System.out.println("Something Went Wrong" + e); 
            } 
            finally 
            { 
                // Clean up clients info and connections
                try 
                {   
                    // Remove client from ConnectedUsers( aka. Online users)
                    ConnectedUsers.remove(myUsername);
                    ClientNum--;
                    //Closing all connection with client
                    in.close(); 
                    out.close(); 
                    myClientSocket.close(); 
                    System.out.println("...Stopped"); 
                } 
                catch(IOException ioe) 
                { 
                    System.out.println("Something Went Wrong when cleaning" + ioe);
                } 
            } 
        } 


    }
}
