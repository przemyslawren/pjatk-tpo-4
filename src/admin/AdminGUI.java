package admin;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class AdminGUI extends JFrame {
    private JTextArea logArea;
    private JTextField topicField, messageField;
    private JButton addButton, removeButton, sendMessageButton;
    private SocketChannel channel;

    public AdminGUI() {
        setTitle("Admin Interface");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));
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

        addButton.addActionListener(e -> manageTopic("add", topicField.getText()));
        removeButton.addActionListener(e -> manageTopic("remove", topicField.getText()));
        sendMessageButton.addActionListener(e -> sendMessage(messageField.getText()));

        try {
            connectToServer();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot connect to server", "Error", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);
    }

    private void connectToServer() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress("localhost", 12345));
        while (!channel.finishConnect()) {
            // Wait for connection to finish
        }
        logArea.append("Connected to server\n");
    }

    private void manageTopic(String command, String topic) {
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Topic field cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String fullCommand = command + " " + topic + "\n";
        sendMessage(fullCommand);
        topicField.setText("");
    }

    private void sendMessage(String msg) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            logArea.append("Command sent: " + msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminGUI::new);
    }
}