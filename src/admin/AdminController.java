package admin;

import java.io.IOException;

public class AdminController {
    private AdminModel model;
    private AdminGUI view;

    public AdminController(AdminModel model, AdminGUI view) {
        this.model = model;
        this.view = view;
        view.addTopicListener(this::manageTopic);
        view.addMessageListener(this::sendMessage);
    }

    public void connect() {
        try {
            model.connect();
            view.showConnected();
        } catch (IOException e) {
            view.showError("Failed to connect: " + e.getMessage());
        }
    }

    private void manageTopic(String command, String topic) {
        try {
            model.sendCommand(command + " " + topic);
            view.updateLog("Topic " + command + ": " + topic);
        } catch (IOException e) {
            view.showError("Error sending topic command: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        try {
            model.sendCommand("send " + message);
            view.updateLog("Message sent: " + message);
        } catch (IOException e) {
            view.showError("Error sending message: " + e.getMessage());
        }
    }
}