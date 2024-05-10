package admin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;
import java.awt.*;

public class AdminGUI extends JFrame {
    private JTextArea logArea;
    private JTextField topicField, messageField;
    private JButton addButton, removeButton, sendMessageButton;

    public AdminGUI() {
        setTitle("Admin Interface");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        topicField = new JTextField();
        messageField = new JTextField();
        addButton = new JButton("Add Topic");
        removeButton = new JButton("Remove Topic");
        sendMessageButton = new JButton("Send Message");
        panel.add(new JLabel("Topic:"));
        panel.add(topicField);
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(new JLabel("Message:"));
        panel.add(messageField);
        panel.add(sendMessageButton);
        add(panel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void addTopicListener(BiConsumer<String, String> listener) {
        addButton.addActionListener(e -> listener.accept("add", topicField.getText()));
        removeButton.addActionListener(e -> listener.accept("remove", topicField.getText()));
    }

    public void addMessageListener(Consumer<String> listener) {
        sendMessageButton.addActionListener(e -> listener.accept(messageField.getText()));
    }

    public void showConnected() {
        JOptionPane.showMessageDialog(this, "Connected to server!", "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateLog(String message) {
        logArea.append(message + "\n");
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}