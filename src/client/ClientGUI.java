package client;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private JTextArea messageArea;
    private JButton refreshTopicsButton;

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

        setVisible(true);
    }

    public void addRefreshListener(Consumer<String> listener) {
        refreshTopicsButton.addActionListener(e -> listener.accept("refresh"));
    }

    public void showConnected() {
        JOptionPane.showMessageDialog(this, "Connected to server!", "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addReceiveListener(Supplier<String> listener) {
        // Implementacja odbioru danych
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}