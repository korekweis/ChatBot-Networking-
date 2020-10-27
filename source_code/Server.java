import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.Timestamp;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private volatile Boolean connect = true;
    private ServerMain serverMain;


    //Constructor
    public Server (int nPort, ServerMain serverMain) {
        this.serverMain = serverMain;

        try {
            serverSocket = new ServerSocket(nPort);
            System.out.println("Server: Listening on port " + nPort + " with IP address " + serverSocket.getLocalSocketAddress()+ "...\n");
        }
        catch (IOException e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(connect){
                Socket serverEndpoint = serverSocket.accept();

                // Create a new handler object for handling this request.
                ClientHandler clientHandler = new ClientHandler(serverEndpoint, serverMain);

                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            System.out.println("Server: Connection terminated!");
        }
    }

    public void stop() {
        try {
            serverSocket.close();
            connect = false;
        } catch (IOException e) {
//            System.out.println(e.getMessage());
        }
    }



}
