import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.net.*;

public class ClientGUI extends JFrame {

    private JPanel panel = new JPanel();
    private JFrame frame = new JFrame("De La Salle Usap (DLSU)");
    private JTextField IPText = new JTextField(15);
    private JTextField portText = new JTextField(15);
    private JButton submitButton = new JButton("Submit");
    private JOptionPane warningMessage = new JOptionPane();

    ClientGUI() {
        frame.setSize(320, 190);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setLocationRelativeTo(null);

        panel.setLayout(null);

        JLabel labelIP = new JLabel("IP Address");
        labelIP.setBounds(20, 20, 120, 25);
        panel.add(labelIP);

        IPText.setBounds(100, 20, 165, 25);
        panel.add(IPText);

        JLabel labelPort = new JLabel("Port Number");
        labelPort.setBounds(20, 50, 120, 25);
        panel.add(labelPort);

        portText.setBounds(100, 50, 165, 25);
        panel.add(portText);

        submitButton.setBounds(170, 90, 100, 25);
        panel.add(submitButton);


        frame.setVisible(true);
    }

    public String getIPNumber() {
        return IPText.getText();
    }

    public String getPortNumber() {
        return portText.getText();
    }

    public JFrame getFrame() {
        return frame;
    }

    public void addButtonListener(ActionListener listenSubmit) {
        submitButton.addActionListener(listenSubmit);
    }

    public void showError (String message, String errorTitle) {
        warningMessage.showMessageDialog(frame, message,errorTitle, JOptionPane.ERROR_MESSAGE);
    }

    public void dispose () {
        frame.dispose();
    }
}
