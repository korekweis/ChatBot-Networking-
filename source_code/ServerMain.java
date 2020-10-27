import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerMain {

    private Thread thread;
    private Server server;
    private ArrayList<String> logs = new ArrayList<String>();
    private ClientHandler[] clientList = new ClientHandler[2];
    private Socket[] fileSharingSocket = new Socket[2];

    public Server getServer(){
        return server;
    }

    public ArrayList<String> getLogs () {
        return logs;
    }

    public ClientHandler[] getClientList () {
        return clientList;
    }

    public Socket[] getFileSharingSocket () {
        return fileSharingSocket;
    }

    public void addFileSharingSocket (Socket fileSocket) {
        if (fileSharingSocket[0] == null) {
            fileSharingSocket[0] = fileSocket;
        } else if (fileSharingSocket[1] == null) {
            fileSharingSocket[1] = fileSocket;
        }
    }

    public void removeFileSharingSocket (Socket fileSocket) {

        try {
            if (fileSharingSocket[0] != null && fileSharingSocket[0].equals(fileSocket)) {
                fileSharingSocket[0].close();
                fileSharingSocket[0] = null;
            } else if (fileSharingSocket[1] != null && fileSharingSocket[1].equals(fileSocket)) {
                fileSharingSocket[1].close();
                fileSharingSocket[1] = null;
            }
        } catch (IOException e) {
//            System.out.println("[FileSharing]: Unable to Close Socket For File Sharing");
        }
    }

    public Socket getOtherFileSharingSocket (Socket self) {
        Socket other = null;

        if (fileSharingSocket[0] != null && !fileSharingSocket[0].equals(self)) {
            other = fileSharingSocket[0];

        } else if (fileSharingSocket[1] != null && !fileSharingSocket[1].equals(self)) {
            other = fileSharingSocket[1];
        }

        return other;
    }

    public ClientHandler getOtherClient (ClientHandler self) {
        ClientHandler other = null;

        if (clientList[0] != null && !clientList[0].equals(self)) {
            other = clientList[0];

        } else if (clientList[1] != null && !clientList[1].equals(self)) {
            other = clientList[1];
        }
        return other;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void startThread () {
        thread.start();
    }

    public String writeLog (SocketAddress source, SocketAddress destination, String activity) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String strTimeStamp = timestamp.toString().split("\\.")[0];

        String log = "Timestamp: " + strTimeStamp + "\nSource: " + source +
                "\nDestination: " + destination + "\nActivity: " + activity + "\n";

        logs.add(log);
        return log;
    }

    public String logsString () {
        String logsString = "";

        for (int i=0;i<logs.size();i++)
            logsString += (logs.get(i) + "\n");

        return logsString;
    }

    public void clearLogs () {
        logs.clear();
    }

    public void askTextFile () {
        if (clientList[0] == null && clientList[1] == null) {
            Scanner sc = new Scanner(System.in);
            Boolean isWrong = true;
            String answer;

            while (isWrong) {
                System.out.println("\nDo you want to save the logs in a text file? [yes/no]");
                answer = sc.nextLine();

                if (answer.equalsIgnoreCase("yes")) {

                    try {
                        FileOutputStream fos = new FileOutputStream("logs.txt");
                        OutputStreamWriter osw = new OutputStreamWriter(fos);
                        BufferedWriter bw = new BufferedWriter(osw);

                        bw.write(logsString());

                        System.out.println("Server: Text file named logs.txt has been saved to the same directory " +
                                "here!");

                        bw.flush();

                        bw.close();
                        osw.close();
                        fos.close();
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }

                    clearLogs();
                    isWrong = false;
                } else if (answer.equalsIgnoreCase("no")) {
                    clearLogs();
                    isWrong = false;
                } else {
                    System.out.println("Invalid Input! Try again.");
                }
            }

            try {
                server.stop();

            } catch (Exception e) {
//                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {

        ServerMain serverMain = new ServerMain();
        int nPort = 4000;

        serverMain.setServer(new Server(nPort, serverMain));

        serverMain.setThread(new Thread(serverMain.getServer()));
        serverMain.startThread();
    }
}
