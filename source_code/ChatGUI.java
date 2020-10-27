import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;

public class ChatGUI {
        private String OS = System.getProperty("os.name").toLowerCase();

        private JFrame frame = new JFrame("De La Salle Usap (DLSU)");
        private JMenuBar menuBar = new JMenuBar();
        private JMenu menuOne = new JMenu("Menu");
        private JMenuItem menuOneLogOut = new JMenuItem("Log Out");
        private JPanel panel = new JPanel(); // the panel is not visible in output
        private JLabel label = new JLabel("Enter your message:");
        private JTextArea sendMessage = new JTextArea(3, 20);
        private JScrollPane scrollSendMessage = new JScrollPane(sendMessage);
        private JButton send = new JButton("Send");
        private JButton sendFile = new JButton("Send File");
        private JTextPane chatInterface = new JTextPane();
        private JScrollPane scrollPane = new JScrollPane(chatInterface);
        private static JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        private String mydownloadfolder;
        private String filename;
        private String newFilename;

        //colors: https://teaching.csse.uwa.edu.au/units/CITS1001/colorinfo.html
        public static final Color PURPLE = new Color(102,0,153);
        public static final Color VERY_DARK_GREEN = new Color(0,120,0);
        public static final Color BLACK = new Color(0,0,0);


    public ChatGUI(JFrame parentClientGuiFrame) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(600, 500));
        frame.setLocationRelativeTo(parentClientGuiFrame);

        send.setActionCommand("send");
        send.setEnabled(true);
        sendFile.setActionCommand("send file");
        sendFile.setEnabled(true);
        menuOneLogOut.setActionCommand("logout");

        //Creating the MenuBar and adding components
        menuBar.add(menuOne);
        menuOne.add(menuOneLogOut);

        //Creating the panel at bottom and adding components
        sendMessage.setLineWrap(true);
        sendMessage.setWrapStyleWord(true);

        scrollSendMessage.setViewportView(sendMessage);

        // Components Added using Flow Layout
        panel.add(label);
        panel.add(scrollSendMessage);
        panel.add(send);
        panel.add(sendFile);

        //Text Area at the Center
        chatInterface.setEditable(false);

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.setResizable(false);
        frame.setVisible(true);

        mydownloadfolder = "";
    }

    public JFrame getFrame() {
        return frame;
    }

    public String getMessage() {
        return sendMessage.getText();
    }

    public void clearMessage() {
        sendMessage.setText("");
    }


    public void addListeners (ActionListener l) {
        menuOneLogOut.addActionListener(l);
        send.addActionListener(l);
        sendFile.addActionListener(l);
    }

    public void appendMessage(String msg) {
        chatInterface.setEditable(true);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attributeSet;
        attributeSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, BLACK);
        attributeSet = sc.addAttribute(attributeSet, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        chatInterface.setCharacterAttributes(attributeSet, true);

        getMessageContent(msg);
        chatInterface.setEditable(false);
    }

    public void appendChat(String msg, Boolean sender) {
        chatInterface.setEditable(true);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attributeSet;
        if (sender)
            attributeSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, PURPLE);
        else
            attributeSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, VERY_DARK_GREEN);

        attributeSet = sc.addAttribute(attributeSet, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        chatInterface.setCharacterAttributes(attributeSet, true);

        getMessageContent(msg);
        chatInterface.setEditable(false);
    }

    public void getMessageContent(String msg){
        int len = chatInterface.getDocument().getLength();
        chatInterface.setCaretPosition(len);
        chatInterface.replaceSelection(msg + "\n\n");
    }

    public void errorMessage(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(frame, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }

    public void enableButton(Boolean enable) {
        send.setEnabled(enable);
        sendFile.setEnabled(enable);
    }

    public void enableSendFileButton(Boolean enable) {
        sendFile.setEnabled(enable);
    }

    public void openFolder() {
        JDialog dialog = new JDialog(frame);

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose directory to save file");

        int nOpen = chooser.showOpenDialog(frame);
//        System.out.println("ChatGUI.java: " + nOpen);

        if (nOpen == JFileChooser.APPROVE_OPTION) {
            mydownloadfolder = chooser.getSelectedFile().toString();

            if (isWindows()) {
                mydownloadfolder += "\\";
            } else if (isMac()) {
                mydownloadfolder += "/";
            } else {
                mydownloadfolder += "\\";
            }

        } else {
            mydownloadfolder = "";
        }

        newFilename = JOptionPane.showInputDialog(frame, "Filename: ", removeExtension(filename));

        //if the user press cancel for rename
        if (newFilename == null) {
            newFilename = removeExtension(filename);
        } else { //if the user press okay
            //if user types empty or types a file name that contains period
            while (newFilename.trim().isEmpty() || newFilename.contains(".")) {
                errorMessage("Filename cannot be empty or containing period.", "Invalid Input!");
//                System.out.println("filename: " + newFilename);
                //            System.out.println("my download folder " + chooser.getSelectedFile());
                //            System.out.println(chooser.getSelectedFile().toString());

                newFilename = JOptionPane.showInputDialog(frame, "Filename: ", removeExtension(filename));

                //if the user press cancel for rename
                if (newFilename == null) {
                    newFilename = removeExtension(filename);
                    break;
                }
            }
        }
    }

    public String getMyDownloadFolder() {
        return this.mydownloadfolder;
    }

    public void dispose () {
        frame.dispose();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getNewFilename() {
        return newFilename;
    }

    public String removeExtension(String fileName) {
        String fileWithoutExtension = "";

        int i = fileName.lastIndexOf('.');

        if (i > 0) {
            fileWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        }

        return fileWithoutExtension;
    }

    public boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }
}
