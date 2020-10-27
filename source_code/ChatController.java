import java.awt.event.*;

public class ChatController implements ActionListener {
    private Client client;
    private ChatGUI chatGui;

    public ChatController (Client client, ChatGUI chatGui)  {
        this.client = client;
        this.chatGui = chatGui;
        chatGui.addListeners(this);
    }

    public void actionPerformed (ActionEvent e) {
        String msgClient;
        if (e.getActionCommand().equals("send")) {
            msgClient = chatGui.getMessage();

            if (!msgClient.trim().isEmpty()) {
                client.sendMessage(msgClient);
                chatGui.clearMessage();
            }
        } else if (e.getActionCommand().equals("send file")) {
            client.changeToFileGUI();
        } else if (e.getActionCommand().equals("logout")) {
            client.logout();
            client.Disconnect();
            client.setIPNumber(null);
            ClientGUI clientGui = new ClientGUI();
            ClientController clientController = new ClientController(new Client(), clientGui);
            chatGui.dispose();
        }
    }
}
