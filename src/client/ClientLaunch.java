package client;

import javax.swing.SwingUtilities;

public class ClientLaunch {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientModel model = new ClientModel("localhost", 12345);
            ClientGUI gui = new ClientGUI();
            ClientController controller = new ClientController(model, gui);
            controller.connect();
        });
    }
}
