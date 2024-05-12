package admin;

import java.io.IOException;

public class AdminController {
    private AdminModel model;
    private AdminGUI view;

    public AdminController(AdminModel model, AdminGUI view) {
        this.model = model;
        this.view = view;
        view.addTopicListener(this::sendCommand);
        view.addMessageListener(this::sendCommand);
    }

    public void connect() {
        try {
            model.connect();
            view.showConnected();
        } catch (IOException e) {
            view.showError("Failed to connect: " + e.getMessage());
        }
    }

    private void sendCommand(String... args) {
        try {
            model.sendCommand(String.join(" ", args));
            view.updateLog(String.join(": ", args));
        } catch (IOException e) {
            view.showError("Error sending command: " + e.getMessage());
        }
    }
}
