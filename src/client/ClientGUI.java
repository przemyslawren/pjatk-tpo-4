package client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientGUI extends JFrame {
    private DefaultListModel<String> topicListModel;
    private JList<String> topicList;
    private JTextArea messageArea;
    private SocketChannel channel;
    private ScheduledExecutorService executorService;

    public ClientGUI() {
        setTitle("Client Subscriber");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        topicListModel = new DefaultListModel<>();
        topicList = new JList<>(topicListModel);
        JScrollPane scrollPane = new JScrollPane(topicList);
        scrollPane.setPreferredSize(new Dimension(200, 0));
        add(scrollPane, BorderLayout.WEST);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        JButton subscribeButton = new JButton("Subscribe");
        JButton unsubscribeButton = new JButton("Unsubscribe");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(subscribeButton);
        buttonPanel.add(unsubscribeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> requestTopicList());
        subscribeButton.addActionListener(e -> updateSubscription("subscribe"));
        unsubscribeButton.addActionListener(e -> updateSubscription("unsubscribe"));

        executorService = Executors.newSingleThreadScheduledExecutor();
        connectToServer();
        requestTopicList();

        setVisible(true);
    }

    private void connectToServer() {
        try {
            channel = SocketChannel.open(new InetSocketAddress("localhost", 12345));
            channel.configureBlocking(false);
            System.out.println("Connected to server");
            startDataReceiver();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDataReceiver() {
        executorService.scheduleAtFixedRate(this::receiveData, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void requestTopicList() {
        sendCommand("list");
    }

    private void updateTopicList(String topics) {
        SwingUtilities.invokeLater(() -> {
            topicListModel.clear();
            for (String topic : topics.split(",")) {
                topicListModel.addElement(topic);
            }
        });
    }

    private void updateSubscription(String type) {
        String selectedTopic = topicList.getSelectedValue();
        if (selectedTopic != null) {
            sendCommand(type + " " + selectedTopic);
        }
    }

    private void receiveData() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            while (channel.read(buffer) > 0) {
                buffer.flip();
                String response = new String(buffer.array(), 0, buffer.limit());
                System.out.println("Received: " + response);
                if (response.contains(",")) {  // Prosty sposób na rozróżnienie listy tematów
                    updateTopicList(response);
                } else {
                    messageArea.append(response + "\n");
                }
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCommand(String command) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap((command + "\n").getBytes(StandardCharsets.UTF_8));
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            System.out.println("Command sent: " + command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda do odbierania danych z serwera powinna być wywoływana przez Selector w osobnym wątku
    // Można użyć ScheduledExecutorService do regularnego sprawdzania danych na kanale

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}