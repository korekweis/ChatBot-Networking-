import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.net.*;

public class ClientController{

    private Client client;
    private ClientGUI clientGui;


    public ClientController (Client client, ClientGUI clientGui) {
        this.client = client;
        this.clientGui = clientGui;

        this.clientGui.addButtonListener(new SubmitButton());
    }

    class SubmitButton implements ActionListener {
        String IPNum;
        int portNum;
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (!clientGui.getIPNumber().trim().isEmpty() && !clientGui.getPortNumber().trim().isEmpty()) {
                    IPNum = clientGui.getIPNumber();
                    portNum = Integer.parseInt(clientGui.getPortNumber());

                    client.setIPNumber(IPNum);
                    client.setPortNumber(portNum);

//                    System.out.println("IPAddress: " + client.getIPNumber());
//                    System.out.println("Port Number: " + client.getPortNumber());

                    ChatGUI chatGui = new ChatGUI(clientGui.getFrame());

                    client.setChatGui(chatGui);
                    Boolean enter = client.Connect();

                    if (enter) {
//                        System.out.println(enter);
                        chatGui.enableButton(true);
                        new Thread(client).start();

                        ChatController chatController = new ChatController(client, chatGui);
                        clientGui.dispose();
                    } else {
                        chatGui.dispose();
                    }
                } else {
                    clientGui.showError("IP Address and Port Number should not be empty.", "Invalid Input");
                }
            } catch (IOException error) {
                clientGui.showError("Something went wrong! Please try again.", "Error");
            } catch (NumberFormatException error) {
                clientGui.showError("Port Number should be a number.", "Invalid Input");
            }
        }
    }
}