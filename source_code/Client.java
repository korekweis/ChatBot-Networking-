import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.net.*;

public class Client implements Runnable{

    private String IPNumber;
    private int portNumber;
    private Socket s;
    private StringTokenizer strTokens;
    private ChatGUI chatGui;
    private DataInputStream dis;
    private DataOutputStream dos;

    public void setIPNumber(String ipNum) {
        IPNumber = ipNum;
    }

    public void setPortNumber(int portNum) {
        portNumber = portNum;
    }

    public String getIPNumber() {
        return IPNumber;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void sendMessage (String message) {
        try {
            chatGui.appendChat(message, true);
            dos.writeUTF("CHAT "+ message);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public String getChangeFilename(String path){
        //placing underscores in the filenames
        File p = new File(path);
//        System.out.println("getChangeFileName: " + path);
        String strFileName = p.getName();
//        System.out.println("getChangeFileName: " + strFileName);
        return strFileName.replace(" ", "_");
    }

    public void sendFile (String file) {
        try {
            file = getChangeFilename(file);
            //going to ClientHandler.java
            chatGui.appendMessage("You sent a file to the other client. The file name is " + file);
            dos.writeUTF("SEND_FILE " + file);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public Boolean Connect() throws IOException {
        try{
            s = new Socket(IPNumber, portNumber);

            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF("CLIENT_JOIN");
            chatGui.appendMessage("Connected to the server!");
            return true;

        } catch (IOException e) {
            chatGui.errorMessage("Unable to Connect to Server, please try again!", "Connecting to the server failed");
            chatGui.appendMessage("[IOException]: "+ e.getMessage());
            return false;
        }
    }

    public void changeToFileGUI() {
        FileSelectGUI fileSelectGui = new FileSelectGUI(this.chatGui);
        FileSelectController fileSelectController = new FileSelectController(this, fileSelectGui);
    }

    public void changeToClientGUI() {
        ClientGUI clientGui = new ClientGUI();
        ClientController clientController = new ClientController(new Client(), clientGui);
    }

    @Override
    //called by clientController line 43, threading starts once IP and host number are placed
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()){
                String message = dis.readUTF();

                strTokens = new StringTokenizer(message);

                String action = strTokens.nextToken();

                switch(action){
                    case "TWO_CLIENT_ON_SERVER":
                        //from ClientHandler.java
                        chatGui.errorMessage("Two clients are already on this server. You cannot connect.",
                                "Server Unavailable");
                        chatGui.dispose();
                        changeToClientGUI();
                        break;
                    case "OTHER_CLIENT_NOT_EXIST":
                        //from ClientHandler.java
                        chatGui.errorMessage("You cannot chat or send file yet unless another client is connected!",
                                "Other Client Unavailable");
                        chatGui.enableSendFileButton(false);
                        break;

                    case "CLIENT_JOINED":
                        //from ClientHandler.java
                        chatGui.appendMessage("The other client connected! You can now start chatting or sending " +
                                "files" +
                                ".");
                        chatGui.enableButton(true);
                        break;

                    case "OTHER_CLIENT_DISCONNECT":
                        //from ClientHandler.java
                        chatGui.errorMessage("You cannot chat or send file yet unless another client is connected!",
                                "Other Client Disconnected");
                        chatGui.appendMessage("Other client disconnected!");
                        chatGui.enableSendFileButton(false);
                        break;

                    case "WRITE_MESSAGE":
                        //from ClientHandler.java
                        String msg = "";

                        while(strTokens.hasMoreTokens()){
                            msg = msg +" "+ strTokens.nextToken();
                        }

//                        System.out.println(msg);
                        chatGui.appendChat(msg, false);
                        break;

                    //  This will inform the client that there's a file receive, Accept or Reject the file
                    case "FILE":
                        //coming from ClientHandler.java

//                        System.out.println("Client.java: " + "[FILE]");
                        String fileName = strTokens.nextToken();

                        int confirm = JOptionPane.showConfirmDialog(chatGui.getFrame(),"\nFilename: "+fileName+
                                "\nwould you like to Accept?");

                        if(confirm == 0){ // client accepted the request, then inform the sender to send the file now
//                            System.out.println("Client.java: Accept (Yes)");
                            chatGui.setFilename(fileName);
                            chatGui.openFolder();
                            try {
                                dos = new DataOutputStream(s.getOutputStream());

//                                System.out.println("Client.java: [SEND_FILE_ACCEPT]");
                                String format = "SEND_FILE_ACCEPT Other client accepted the file you send!";
                                dos.writeUTF(format);

                                //  this will create a filesharing socket to handle incoming file and this socket
                                //  will automatically closed when it's done.
                                Socket fSoc = new Socket(IPNumber, portNumber);
                                DataOutputStream fdos = new DataOutputStream(fSoc.getOutputStream());
                                fdos.writeUTF("SHARINGSOCKET "); //send to ClientHandler.java

                                new Thread(new ReceivingFileThread(fSoc, chatGui)).start();
                            } catch (IOException e) {
//                                System.out.println("[SEND_FILE]: "+e.getMessage());
                            }
                        } else { // client rejected the request, then send back result to sender
//                            System.out.println("Client.java: Pressed No or Cancel");
                            try {
                                dos = new DataOutputStream(s.getOutputStream());

                                String format = "SEND_FILE_ERROR Client rejected your request or connection was lost!";
                                dos.writeUTF(format); //sending it to ClientHandler.java
                            } catch (IOException e) {
//                                System.out.println("Client.java: [SEND_FILE]: "+e.getMessage());
                            }
                        }
                        break;

                    default:
                        chatGui.appendMessage("Unknown Action "+ action);
                        break;
                }
            }
        } catch(IOException e){
            chatGui.appendMessage(" Server Connection was lost, please try again later!");
        }
    }
    
    public void setChatGui(ChatGUI gui) {
        chatGui = gui;
    }
    
    public ChatGUI getChatGui() {
        return chatGui;
    }

    public void logout () {
        try {
            dos.writeUTF("LOG_OUT");
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void Disconnect() {
        try {
            this.s.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }
}
