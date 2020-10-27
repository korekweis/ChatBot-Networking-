import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.StringTokenizer;

//TODO: REMOVE (SIMILAR with SocketThread of the Filipino)
public class ClientHandler implements Runnable {
    private DataInputStream disReader;
    private Socket serverEndpoint;
    private StringTokenizer strTokens;
    private ServerMain serverMain;
    private String log = "";

    private final int BUFFER_SIZE = 100;

    // constructor
    public ClientHandler(Socket s, ServerMain server) {
        serverEndpoint = s;
        serverMain = server;

        try {
            disReader = new DataInputStream(s.getInputStream());
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return serverEndpoint;
    }

    private void createConnection(String filename){
        try {
            if (serverMain.getClientList()[0] != null && !serverMain.getClientList()[0].equals(this)) {
                //another client
//                System.out.println("ClientHandler.java: " + "in here");

                DataOutputStream dosWriter =
                        new DataOutputStream(serverMain.getClientList()[0].getSocket().getOutputStream());

                String action = "FILE " + filename; //going to Client.java
                dosWriter.writeUTF(action);

            } else if (serverMain.getClientList()[1] != null && !serverMain.getClientList()[1].equals(this)) {
                //another client
//                System.out.println("ClientHandler.java: " + "another in here");

                DataOutputStream dosWriter =
                        new DataOutputStream(serverMain.getClientList()[1].getSocket().getOutputStream());

                String action = "FILE " + filename; //going to Client.java
                dosWriter.writeUTF(action);
            } else {
                //own
                DataOutputStream dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());
                //going to Client.java line 170
                dosWriter.writeUTF("OTHER_CLIENT_DISCONNECT Client has disconnected!");
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String errorMessage = "";
        try {
            while(true){
                String message = disReader.readUTF();
                strTokens = new StringTokenizer(message);
                String action = strTokens.nextToken();

                switch(action){
                    case "CLIENT_JOIN":

                        if (serverMain.getClientList()[0] == null) {
                            serverMain.getClientList()[0] = this;
                        } else if (serverMain.getClientList()[1] == null) {
                            serverMain.getClientList()[1] = this;
                        } else { //meaning more than two clients joined on the same sever
                            DataOutputStream clientDosWriter = new DataOutputStream(this.getSocket().getOutputStream());
                            clientDosWriter.writeUTF("TWO_CLIENT_ON_SERVER");
                           serverEndpoint.close();
                            break;
                        }

                        log = serverMain.writeLog(serverEndpoint.getRemoteSocketAddress(),
                                serverEndpoint.getLocalSocketAddress(),
                                "Client logging in");

                        System.out.println(log);

                        ClientHandler otherClient = serverMain.getOtherClient(this);

                        if (otherClient == null) {
                            //send this to yourself that there is no other client to tell that you joined
                            DataOutputStream joinDosWriter = new DataOutputStream(this.getSocket().getOutputStream());
                            joinDosWriter.writeUTF("OTHER_CLIENT_NOT_EXIST");
                        } else {
                            //send this to the other client that you joined
                            DataOutputStream joinDosWriter = new DataOutputStream(otherClient.getSocket().getOutputStream());
                            joinDosWriter.writeUTF("CLIENT_JOINED");
                        }

                        break;

                    case "LOG_OUT":
                        try {
                            if (serverMain.getClientList()[0] != null && serverMain.getClientList()[0].equals(this)) {
                               serverEndpoint.close();
                                serverMain.getClientList()[0] = null;

                                if (serverMain.getClientList()[1] != null) {
                                    DataOutputStream dosWriter = new DataOutputStream(serverMain.getClientList()[1].getSocket().getOutputStream());
                                    dosWriter.writeUTF("OTHER_CLIENT_DISCONNECT");
                                }
                            } else if (serverMain.getClientList()[1] != null && serverMain.getClientList()[1].equals(this)) {
                                serverEndpoint.close();
                                serverMain.getClientList()[1] = null;

                                if (serverMain.getClientList()[0] != null) {
                                    DataOutputStream dosWriter = new DataOutputStream(serverMain.getClientList()[0].getSocket().getOutputStream());
                                    dosWriter.writeUTF("OTHER_CLIENT_DISCONNECT");
                                }
                            }

                            log = serverMain.writeLog(this.serverEndpoint.getRemoteSocketAddress(), this.serverEndpoint.getLocalSocketAddress(), "Client logging out");

                            System.out.println(log);

                            //asking server if saving to a text file
                            serverMain.askTextFile();
                        } catch (IOException e) {
//                            e.printStackTrace();
                        }
                        break;


                    case "CHAT":
                        //From Client.java line 35
                        String msg = "";

                        while(strTokens.hasMoreTokens()){
                            msg = msg +" "+ strTokens.nextToken();
                        }

                        Socket toSend = null;

                        //get the other client
                        if (serverMain.getClientList()[0] != null && !serverMain.getClientList()[0].equals(this)) {
                            toSend = serverMain.getClientList()[0].getSocket();
                        } else if (serverMain.getClientList()[1] != null && !serverMain.getClientList()[1].equals(this)) {
                            toSend = serverMain.getClientList()[1].getSocket();
                        }

                        if (toSend != null) {
                            try {
                                DataOutputStream dos = new DataOutputStream(toSend.getOutputStream());
                                String content = msg;
                                //found in client line 111
                                dos.writeUTF("WRITE_MESSAGE " + content);

                                log = serverMain.writeLog(serverEndpoint.getRemoteSocketAddress(), toSend.getRemoteSocketAddress(), "Client sent a message");
                                System.out.println(log);

                                //                       // client receives message from this client
                                log = serverMain.writeLog(toSend.getRemoteSocketAddress(), serverEndpoint.getRemoteSocketAddress(), "Client received a message");
                                System.out.println(log);

                            } catch (IOException e) {
//                                e.printStackTrace();
                            }
                        } else {
                            try {
                                DataOutputStream dos = new DataOutputStream(serverEndpoint.getOutputStream());
                                //found in client line 111

                                dos.writeUTF("OTHER_CLIENT_DISCONNECT");
                            } catch (IOException e) {
//                                e.printStackTrace();
                            }
                        }
                        break;


                    case "SHARINGSOCKET":
                        //coming from Client.java and FileSelectGUI.java
//                        System.out.println("ClientHandler.java: [SHARRINGSOCKET]");

//                        main.appendMessage("SHARINGSOCKET : Client establish a socket connection for file
//                        sharing...");

                        serverMain.addFileSharingSocket(serverEndpoint);
//                        main.appendMessage("SHARINGSOCKET : File sharing is now open");
                        break;

                    case "SENDFILE":
                        //coming from SendingFileThread.java
//                        System.out.println("ClientHandler.java: [SENDFILE]");

                        String fileName = strTokens.nextToken();
                        String fileSize = strTokens.nextToken();

                        //get the other client
                        Socket clientSocket = serverMain.getOtherFileSharingSocket(this.getSocket());

                        if (clientSocket != null) { //if client exist
                            try {
//                               //sending file to client

                                DataOutputStream clientDosWriter = new DataOutputStream(clientSocket.getOutputStream());
                                //going to ReceivingFileThread
                                clientDosWriter.writeUTF("SENDFILE "+ fileName +" "+ fileSize);

                                log = serverMain.writeLog(serverEndpoint.getRemoteSocketAddress(),
                                        clientSocket.getRemoteSocketAddress(),
                                        "Client sending a file");

                                System.out.println(log);

                                log = serverMain.writeLog(clientSocket.getRemoteSocketAddress(),
                                        serverEndpoint.getRemoteSocketAddress(),
                                        "Client receiving the file");

                                System.out.println(log);

                                InputStream input = serverEndpoint.getInputStream();
                                OutputStream sendFile = clientSocket.getOutputStream();

                                byte[] buffer = new byte[BUFFER_SIZE];
                                int cnt;

                                while((cnt = input.read(buffer)) > 0){
                                    sendFile.write(buffer, 0, cnt);
                                }

                                sendFile.flush();
                                sendFile.close();

                                serverMain.removeFileSharingSocket(clientSocket);
                                serverMain.removeFileSharingSocket(this.getSocket());

                            } catch (IOException e) {
//                                System.out.println("[SENDFILE]: "+ e.getMessage());
                            }
                        } else {
//                            System.out.println("SENDFILE : Client was not found.!");

                            serverMain.removeFileSharingSocket(this.getSocket());

                            DataOutputStream dos = new DataOutputStream(serverEndpoint.getOutputStream());
                            dos.writeUTF("SENDFILEERROR "+ "Client was not found, File Sharing will " +
                                    "exit" +
                                    ".");
                        }
                        break;


                    case "SENDFILERESPONSE":
                        //coming from ReceivingFileThread.java (connection was lost yung error message)
//                        System.out.println("ClientHandler.java: [SENDFILERESPONSE]");

                        errorMessage = "";

                        while(strTokens.hasMoreTokens()){
                            errorMessage = errorMessage + " " + strTokens.nextToken();
                        }

                        try {
                            Socket receiveSocket = serverMain.getOtherFileSharingSocket(this.getSocket());

                            DataOutputStream receiveDosWriter = new DataOutputStream(receiveSocket.getOutputStream());

                            receiveDosWriter.writeUTF("SENDFILERESPONSE" +" "+ errorMessage);
                        } catch (IOException e) {
//                           System.out.println("[SENDFILERESPONSE]: "+ e.getMessage());
                        }
                        break;


                    case "SEND_FILE":
                        //coming from the Client.java
//                        System.out.println("ClientHandler.java: [SEND_FILE]");
                        try {
                            String send_filename = strTokens.nextToken();

                            //sends FILE to the receiving client for them to accept
                            this.createConnection(send_filename);
                        } catch (Exception e) {
//                            System.out.println("[SEND_FILE]: "+ e.getLocalizedMessage());
                        }
                        break;


                    case "SEND_FILE_ERROR":
                        //coming from the Client.java
                        //this one send error to the one who shared file
//                        System.out.println("ClientHandler.java: [SEND_FILE_ERROR]");
                        errorMessage = "";

                        while (strTokens.hasMoreTokens()){
                            errorMessage = errorMessage + " " + strTokens.nextToken();
                        }

                        try {
                            //get the other client because the current client here is the receiver
                            // get the file sharing host socket for connection
                            Socket eSocket = serverMain.getOtherFileSharingSocket(this.getSocket());
                            DataOutputStream eDos = new DataOutputStream(eSocket.getOutputStream());

                            log = serverMain.writeLog(serverEndpoint.getRemoteSocketAddress(),
                                    eSocket.getRemoteSocketAddress(),
                                    "Client did not receive the file");

                            System.out.println(log);

                            //telling the sender that the other client rejected
                            //going to FileSelectGUI.java
                            eDos.writeUTF("RECEIVE_FILE_ERROR "+ errorMessage);
                        } catch (IOException e) {
//                            System.out.println("[RECEIVE_FILE_ERROR]: "+ e.getMessage());
                        }
                        break;


                    case "SEND_FILE_ACCEPT":
                        //coming from Client.java
//                        System.out.println("ClientHandler.java: [SEND_FILE_ACCEPT]");
                        String acceptMessage = "";

                        while(strTokens.hasMoreTokens()){
                            acceptMessage = acceptMessage+" "+strTokens.nextToken();
                        }

                        try {
                            //get the other client
                            //get the file sharing host socket for connection
                            Socket aSocket = serverMain.getOtherFileSharingSocket(this.getSocket());
                            DataOutputStream aDos = new DataOutputStream(aSocket.getOutputStream());

//                            System.out.println("ClientHandler.java: [RECEIVE_FILE_ACCEPT]");
                            //going to FileSelectGUI.java
                            aDos.writeUTF("RECEIVE_FILE_ACCEPT "+ acceptMessage);
                        } catch (IOException e) {
//                            System.out.println("[RECEIVE_FILE_ERROR]: "+ e.getMessage());
                        }
                        break;


                    default:
                        System.out.println("Unknown Action "+ action);
                        break;
                }
            }
        } catch (Exception e) {
            serverMain.removeFileSharingSocket(this.getSocket());
//            System.out.println("Server: Socket connection closed!\n");
        }
    }
}