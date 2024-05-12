package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class ClientModel {
    private SocketChannel channel;
    private Selector selector;
    private String serverAddress;
    private int serverPort;

    public ClientModel(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.connect(new InetSocketAddress(serverAddress, serverPort));
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            throw new IOException("Failed to connect to server", e);
        }
    }

    public void processEvents() throws IOException {
        while (true) {
            selector.select();  // This call blocks until there are events ready
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isConnectable()) {
                    finishConnection(key);
                } else if (key.isReadable()) {
                    receiveData(key);
                }
            }
        }
    }

    private void finishConnection(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.finishConnect()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public String receiveMessage() throws IOException {
        selector.select();

        SelectionKey key = channel.keyFor(selector);
        if (key.isReadable()) {
            return receiveData(key);
        }
        return null;
    }

    private String receiveData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        if (read == -1) {
            channel.close();
            key.cancel();
            return null;
        }
        buffer.flip();
        String response = StandardCharsets.UTF_8.decode(buffer).toString();
        buffer.clear();
        return response;
    }


    public void sendCommand(String command) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap((command + "\n").getBytes(StandardCharsets.UTF_8));
            channel.write(buffer);
        } catch (IOException e) {
            throw new IOException("Failed to send command", e);
        }
    }

    public void close() throws IOException {
        if (selector != null) {
            selector.close();
        }
        if (channel != null) {
            channel.close();
        }
    }

}
