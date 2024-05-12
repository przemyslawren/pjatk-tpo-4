package client;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private JTextArea messageArea;
    public JButton refreshTopicsButton, subscribeButton, unsubscribeButton;
    private JList<String> topicList;
    private DefaultListModel<String> topicListModel;

    public ClientGUI() {
        setTitle("Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        refreshTopicsButton = new JButton("Refresh");
        panel.add(refreshTopicsButton);
        add(panel, BorderLayout.SOUTH);

        topicListModel = new DefaultListModel<>();
        topicList = new JList<>(topicListModel);
        add(new JScrollPane(topicList), BorderLayout.WEST);

        subscribeButton = new JButton("Subscribe");
        unsubscribeButton = new JButton("Unsubscribe");
        panel.add(subscribeButton);
        panel.add(unsubscribeButton);

        setVisible(true);
    }

    public void addRefreshListener(Runnable listener) {
        refreshTopicsButton.addActionListener(e -> {
            System.out.println("Refresh button clicked");
            listener.run();
        });
    }

    public void showConnected() {
        JOptionPane.showMessageDialog(this, "Connected to server!", "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addSubscriptionListener(BiConsumer<String, String> listener) {
        subscribeButton.addActionListener(e -> {
            String selectedTopic = topicList.getSelectedValue();
            if (selectedTopic != null) {
                listener.accept("subscribe", selectedTopic);
            }
        });
        unsubscribeButton.addActionListener(e -> {
            String selectedTopic = topicList.getSelectedValue();
            if (selectedTopic != null) {
                listener.accept("unsubscribe", selectedTopic);
            }
        });
    }

    public void updateTopics(String[] topics) {
        topicListModel.clear();
        for (String topic : topics) {
            topicListModel.addElement(topic);
        }
    }

    public void addReceiveListener(Supplier<String> listener) {
        new Thread(() -> {
            while (true) {
                String message = listener.get();
                if (message != null) {
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append(message + "\n");
                    });
                }
            }
        }).start();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}