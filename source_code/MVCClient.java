public class MVCClient {
    public static void main (String[] args) {
        Client client = new Client();
        ClientGUI clientGUI = new ClientGUI();
        ClientController clientController = new ClientController(client, clientGUI);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (client.getIPNumber() != null) {
                    
                    client.logout();
                    client.Disconnect();
                }
            }
        }));
    }
}
