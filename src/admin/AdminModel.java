package admin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;

public class AdminModel {
    private SocketChannel channel;
    private String serverAddress;
    private int serverPort;

    public AdminModel(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(serverAddress, serverPort));
        while (!channel.finishConnect()) {
            // Wait for connection to complete
        }
    }

    public void sendCommand(String command) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((command + "\n").getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }
}
