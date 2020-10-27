import java.io.*;
import javax.swing.*;
import java.util.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.filechooser.*;


public class FileSelectGUI extends JFrame {

    private String IPNumber;
    private int portNumber;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String file;
    private ChatGUI chatGui;

    JFrame frame = new JFrame("File Search");
    private JPanel panel = new JPanel();
    JTextField pathText = new JTextField(50);
    JButton openButton = new JButton("Browse");
    JButton saveButton = new JButton("Send File");

    public FileSelectGUI(ChatGUI chatGui) {
        this.chatGui = chatGui;
        frame.setSize(620, 150);
        frame.add(panel);
        frame.setLocationRelativeTo(chatGui.getFrame());

        panel.setLayout(null);

        JLabel pathName = new JLabel("path:");
        pathName.setBounds(60, 20, 100, 25);
        panel.add(pathName);

        pathText.setBounds(100, 20, 450, 25);
        panel.add(pathText);

        openButton.setBounds(340, 60, 100, 25);
        saveButton.setBounds(440, 60, 100, 25);

        openButton.setActionCommand("browse");
        saveButton.setActionCommand("send file");

        panel.add(openButton);
        panel.add(saveButton);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public void setFile(String fileName) {

        file = fileName;
    }

    public void addButtonListener(ActionListener listen) {
        openButton.addActionListener(listen);
        saveButton.addActionListener(listen);
    }

    public void setPath(String string) {
        pathText.setText(string);
    }

    public String getPath() { return pathText.getText(); }

    public ChatGUI getChatGui() { return chatGui; }

    public JFrame getFrame() {
        return frame;
    }

    public void disableGUI(Boolean disable){
        if(disable){ // Disable
            pathText.setEditable(false);
            openButton.setEnabled(false);
            saveButton.setEnabled(false);
        } else { // Enable
            pathText.setEditable(true);
            openButton.setEnabled(true);
            saveButton.setEnabled(true);
        }
    }

    public void setTitle(String title) {
        frame.setTitle(title);
    }

    public void successMessage (String successMessage, String successTitle) {
        JOptionPane.showMessageDialog(chatGui.getFrame(), successMessage, successTitle, JOptionPane.INFORMATION_MESSAGE);
    }

    public void errorMessage (String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(chatGui.getFrame(), errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }

    public void dispose () {
        frame.dispose();
    }

    public void connectFile (String IP, int port) {
        this.IPNumber = IP;
        this.portNumber = port;

        try {
            socket = new Socket(IPNumber, portNumber);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            String format = "SHARINGSOCKET ";
            dos.writeUTF(format); //going to ClientHandler.java
//            System.out.println("FileSelectGUI:java: " + format);

            new Thread(new SendFileThread(this)).start();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    class SendFileThread implements Runnable{

        private FileSelectGUI fileSelect;
        private StringTokenizer strTokens;


        public SendFileThread(FileSelectGUI fileSelect){
            this.fileSelect = fileSelect;
        }

        private void closeMe(){
            try {
                socket.close();
            } catch (IOException e) {
//                System.out.println("[Socket Close]: "+e.getMessage());
            }
            dispose();
        }

        @Override
        public void run() {
            String errorMessage = "";
            try {
                while(!Thread.currentThread().isInterrupted()){
                    String data = dis.readUTF();  // Read the content of the received data from the server
                    strTokens = new StringTokenizer(data);
                    String cmd = strTokens.nextToken();  //  Get the first word from the data
                    switch(cmd){
                        case "RECEIVE_FILE_ERROR":
                            //coming from ClientHandler.java line 303
                            //telling the sender that other client rejected the file

                            String message = "";

                            while(strTokens.hasMoreTokens()){
                                message = message + " " + strTokens.nextToken();
                            }

                            fileSelect.errorMessage(message, "Error");
                            fileSelect.chatGui.enableSendFileButton(true);
                            this.closeMe();
                            break;

                        case "RECEIVE_FILE_ACCEPT":
                            //coming from ClientHandler.java
//                            System.out.println("FileSelectGUI.java: " + "[RECEIVE_FILE_ACCEPT]");

                            //the one who is sending the file
                            new Thread(new SendingFileThread(socket, file, fileSelect)).start();
                            break;

                        case "SENDFILEERROR":
                            //coming from ClientHandler.java
                            errorMessage = "";

                            while(strTokens.hasMoreTokens()){
                                errorMessage = errorMessage + " " + strTokens.nextToken();
                            }

//                            System.out.println("SENDFILERROR: " + errorMessage);

                            fileSelect.errorMessage(errorMessage, "Error");
                            fileSelect.disableGUI(false);
                            break;

                        case "SENDFILERESPONSE":
                            //coming from ReceivingFileThread.java
                            errorMessage = "";

                            while(strTokens.hasMoreTokens()){
                                errorMessage = errorMessage + " " + strTokens.nextToken();
                            }

                            //TODO: CHECK IF NEEDED FOR ATTACHMENT
//                            form.updateAttachment(false);
                            fileSelect.errorMessage(errorMessage, "Error");
                            dispose();
                            break;
                    }
                }
            } catch (IOException e) {
//                System.out.println(e.getMessage());
            }
        }
    }
}
