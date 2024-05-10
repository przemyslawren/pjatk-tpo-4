package client;

import java.io.IOException;

public class ClientController {
    private ClientModel model;
    private ClientGUI view;

    public ClientController(ClientModel model, ClientGUI view) {
        this.model = model;
        this.view = view;
        this.view.addRefreshListener(command -> {
            try {
                model.sendCommand(command);
            } catch (IOException e) {
                view.showError("Failed to send command: " + e.getMessage());
            }
        });
        this.view.addReceiveListener(() -> {
            try {
                return model.receiveData();
            } catch (IOException e) {
                view.showError("Failed to receive data: " + e.getMessage());
                return null;
            }
        });
    }

    public void connect() {
        try {
            model.connect();
            view.showConnected();
        } catch (IOException e) {
            view.showError("Connection failed: " + e.getMessage());
        }
    }
}
