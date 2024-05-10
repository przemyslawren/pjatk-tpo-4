package admin;

import java.util.function.BiConsumer;
import javax.swing.*;
import java.awt.*;
import util.TriConsumer;

public class AdminGUI extends JFrame {
    private JTextArea logArea;
    private JTextField topicField;
    private JTextArea messageField;
    private JButton addButton, removeButton, sendMessageButton;
    private DefaultListModel<String> topicListModel;
    private JList<String> topicList;

    public AdminGUI() {
        setTitle("Admin Interface");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topicListPanel = new JPanel(new BorderLayout());
        topicListModel = new DefaultListModel<>();
        topicList = new JList<>(topicListModel);
        JScrollPane topicScrollPane = new JScrollPane(topicList);
        topicListPanel.add(new JLabel("Topics:"), BorderLayout.NORTH);
        topicListPanel.add(topicScrollPane, BorderLayout.CENTER);
        topicListPanel.setPreferredSize(new Dimension(340, 300));
        add(topicListPanel, BorderLayout.WEST);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextArea();
        sendMessageButton = new JButton("Send Message");
        messagePanel.add(new JLabel("Message:"), BorderLayout.NORTH);
        messagePanel.add(new JScrollPane(messageField), BorderLayout.CENTER);
        messagePanel.add(sendMessageButton, BorderLayout.SOUTH);
        messagePanel.setPreferredSize(new Dimension(340, 300));
        add(messagePanel, BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        logPanel.add(new JLabel("Log:"), BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        logPanel.setPreferredSize(new Dimension(340, 300));
        add(logPanel, BorderLayout.EAST);

        JPanel topicPanel = new JPanel(new GridLayout(1, 4));
        topicField = new JTextField();
        addButton = new JButton("Add Topic");
        removeButton = new JButton("Remove Topic");
        topicPanel.add(new JLabel("New Topic:"));
        topicPanel.add(topicField);
        topicPanel.add(addButton);
        topicPanel.add(removeButton);
        add(topicPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void addTopicListener(BiConsumer<String, String> listener) {
        addButton.addActionListener(e -> {
            String topic = topicField.getText();
            if (!topic.isEmpty()) {
                topicListModel.addElement(topic);
                listener.accept("add_topic", topic);
                topicField.setText("");
            } else {
                showError("Provide a name to add new topic!");
            }
        });
        removeButton.addActionListener(e -> {
            String selectedTopic = topicList.getSelectedValue();
            if (selectedTopic != null) {
                topicListModel.removeElement(selectedTopic);
                listener.accept("remove_topic", selectedTopic);
            } else {
                showError("Select topic from list to remove it!");
            }
        });
    }

    public void addMessageListener(TriConsumer<String, String, String> listener) { // add selected topic
        sendMessageButton.addActionListener(e -> {
            String selectedTopic = topicList.getSelectedValue();
            if (selectedTopic != null && !messageField.getText().isEmpty()) {
                listener.accept("send_message", messageField.getText(), selectedTopic);
                messageField.setText("");
            } else {
                showError("Select a topic and enter a message!");
            }
        });
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