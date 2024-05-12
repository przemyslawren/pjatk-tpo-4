package client;

import java.io.IOException;
import java.util.Arrays;
import javax.swing.SwingUtilities;

public class ClientController {
    private ClientModel model;
    private ClientGUI view;

    public ClientController(ClientModel model, ClientGUI view) {
        this.model = model;
        this.view = view;// Add this line
    }

    public void connect() {
        new Thread(() -> {
            try {
                model.connect();
                initListeners();
                view.showConnected();
                model.processEvents();
            } catch (IOException e) {
                view.showError("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    private void sendRefreshCommand() {
        try {
            model.sendCommand("refresh");
            String response = model.receiveMessage();
            System.out.println("Received response: " + response);
            if (response != null) {
                System.out.println(Arrays.toString(response.split(",")));
                String[] topics = response.split(",");
                SwingUtilities.invokeLater(() -> view.updateTopics(topics));
            }
        } catch (IOException e) {
            view.showError("Failed to refresh topics: " + e.getMessage());
        }
    }

    private void initListeners() {
        view.addRefreshListener(this::sendRefreshCommand);

        view.addSubscriptionListener((command, topic) -> {
            try {
                model.sendCommand(command + " " + topic);
            } catch (IOException e) {
                view.showError("Failed to send command: " + e.getMessage());
            }
        });

        view.addReceiveListener(() -> {
            try {
                String message = model.receiveMessage();
                if(message == null) {
                    return null;
                }
                System.out.println("Received message: " + message);
                return message;
            } catch (IOException e) {
                view.showError("Failed to receive message: " + e.getMessage());
                return null;
            }
        });
    }

//    private void startReceivingEvents() {
//        new Thread(() -> {
//            try {
//                while (true) {
//                    String message = model.receiveMessage();
//                    if (message != null) {
//                        if (message.startsWith("REFRESH")) {
//                            // Assuming the message is in the format "TOPICS topic1,topic2,topic3,..."
//                            String[] topics = message.substring(7).split(",");
//                            SwingUtilities.invokeLater(() -> updateTopics(topics));
//                        } else {
//                            SwingUtilities.invokeLater(() -> view.addReceiveListener(() -> message));
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                SwingUtilities.invokeLater(() -> view.showError("Failed to process events: " + e.getMessage()));
//            }
//        }).start();
//    }

    public void updateTopics(String[] topics) {
        view.updateTopics(topics);
    }
}
