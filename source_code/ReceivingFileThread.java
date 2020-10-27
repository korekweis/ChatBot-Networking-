import java.util.*;
import java.io.*;
import javax.swing.*;
import java.net.*;

public class ReceivingFileThread implements Runnable {
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected ChatGUI chatGui;
    protected StringTokenizer strTokens;
    private final int BUFFER_SIZE = 100;

    public ReceivingFileThread(Socket soc, ChatGUI chatGui){
        this.socket = soc;
        this.chatGui = chatGui;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
//            System.out.println("[ReceivingFile]: " +e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()){
                String data = dis.readUTF();
                strTokens = new StringTokenizer(data);
                String action = strTokens.nextToken();

                switch(action){

                    //   This will handle the receiving of a file in background process from other user
                    case "SENDFILE":
                        try {
                            String filename = strTokens.nextToken();
                            String newFilename = chatGui.getNewFilename();
                            String extension = getExtension(filename);
//                            System.out.println("ReceivingFileThread: " + filename);

//                            System.out.println("Printing extension: " + getExtension(filename));

                            newFilename = newFilename + "." + extension;
//                            System.out.println("Testing");
                            int filesize = Integer.parseInt(strTokens.nextToken());
                           // chatGui.setMyTitle("Downloading File....");

                            //Coming from SendingFileThread.java
//                            System.out.println("ReceivingFileThread.java: [SENDFILE]");
//                            System.out.println("Downloading File....");

                            String path = chatGui.getMyDownloadFolder() + newFilename;

                            FileOutputStream fos = new FileOutputStream(path);
                            InputStream input = socket.getInputStream();

                            ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(chatGui.getFrame(),
                                    "Downloading file please wait...", input);
                            BufferedInputStream bis = new BufferedInputStream(pmis);

                            // Create a temporary file
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int count, percent = 0;

                            while((count = bis.read(buffer)) != -1){
                                percent = percent + count;
//                                int p = (percent / filesize);
//                                chatGui.setMyTitle("Downloading File  "+ p +"%");
                                fos.write(buffer, 0, count);
                            }

                            fos.flush();
                            fos.close();

                            if (chatGui.getMyDownloadFolder() != "") {
                                JOptionPane.showMessageDialog(chatGui.getFrame(), "File has been downloaded to \n'" + path + "'");
                                chatGui.appendMessage("You accepted the file which the other client sent. File has been" + " downloaded to \n'" + path + "'");
                            } else if (chatGui.getMyDownloadFolder() == "") {
                                JOptionPane.showMessageDialog(chatGui.getFrame(),
                                        "File has been downloaded inside the folder containing the java files with " +
                                                "the filename:\n'" + path + "'");
                                chatGui.appendMessage("You accepted the file which the other client sent. File has " +
                                        "been downloaded inside the folder containing the java files with the " +
                                        "filename:\n'" + path + "'");
                            }
                        } catch (IOException e) {
                            DataOutputStream eDos = new DataOutputStream(socket.getOutputStream());
                            eDos.writeUTF("SENDFILERESPONSE Connection was lost, please try again later!");

//                            System.out.println(e.getMessage());
                            JOptionPane.showMessageDialog(chatGui.getFrame(), e.getMessage(), "Exception",
                                    JOptionPane.ERROR_MESSAGE);
                            socket.close();
                        }
                        break;
                }
            }
        } catch (Exception e) {
//            System.out.println("ReceivingFileThread: [ReceivingFile]: " +e.getMessage());
        }
    }

    public String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }
}


