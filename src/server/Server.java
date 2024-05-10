package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server {
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private Map<String, Set<SocketChannel>> topicSubscribers;  // Mapuje tematy na subskrybentów
	private Map<SocketChannel, Set<String>> clientSubscriptions;  // Mapuje klientów na ich subskrypcje

	public Server(int port) throws IOException {
		this.selector = Selector.open();
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.configureBlocking(false);
		this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.topicSubscribers = new HashMap<>();
		this.clientSubscriptions = new HashMap<>();
	}

	// Metody start, handleAccept, handleRead itp. będą zaimplementowane później

	public static void main(String[] args) {
		try {
			Server server = new Server(12345);
			server.start();  // Startowanie serwera (metoda do implementacji)
		} catch (IOException e) {
			System.err.println("Failed to start the server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			System.out.println("Server started on port " + serverSocketChannel.socket().getLocalPort());
			while (true) {
				selector.select();  // Blokowanie, aż jakiś kanał nie będzie gotowy do I/O
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();

				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();  // Usunięcie klucza z zestawu, aby uniknąć ponownego przetwarzania

					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						handleAccept();
					} else if (key.isReadable()) {
						handleRead(key);
					} else if (key.isWritable()) {
						handleWrite(key);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleWrite(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();  // Załóżmy, że wcześniej dołączyliśmy bufor z danymi do wysłania

		if (buffer != null) {
			clientChannel.write(buffer);
			// Sprawdź, czy bufor został całkowicie opróżniony
			if (!buffer.hasRemaining()) {
				buffer.compact();  // Przygotowanie bufora do kolejnego zapisu
				key.interestOps(SelectionKey.OP_READ);  // Rejestracja kanału do odczytu, jeśli już nie mamy czego pisać
			}
		}
	}

	private void handleAccept() throws IOException {
		SocketChannel clientChannel = serverSocketChannel.accept();
		if (clientChannel != null) {
			clientChannel.configureBlocking(false);
			clientChannel.register(selector, SelectionKey.OP_READ);
			clientSubscriptions.put(clientChannel, new HashSet<>());
			System.out.println("Accepted connection from " + clientChannel.getRemoteAddress());
		}
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int read = clientChannel.read(buffer);
		if (read == -1) {
			clientSubscriptions.remove(clientChannel);
			clientChannel.close();
		} else {
			buffer.flip();
			String command = new String(buffer.array(), 0, buffer.limit()).trim();
			System.out.println("Received: " + command);
			processCommand(clientChannel, command);
		}
	}

	private void processCommand(SocketChannel clientChannel, String command) throws IOException {
		if (command.equals("list")) {
			String topics = String.join(",", topicSubscribers.keySet());
			ByteBuffer buffer = ByteBuffer.wrap(topics.getBytes());
			clientChannel.write(buffer);
		} else if (command.startsWith("subscribe")) {
			String topic = command.split(" ")[1];
			subscribeClientToTopic(clientChannel, topic);
		} else if (command.startsWith("unsubscribe")) {
			String topic = command.split(" ")[1];
			unsubscribeClientFromTopic(clientChannel, topic);
		} else if (command.startsWith("send")) {
			String[] parts = command.split(" ", 3);
			if (parts.length > 2) {
				String topic = parts[1];
				String message = parts[2];
				sendMessageToTopic(topic, message);
			}
		}
	}

	private void subscribeClientToTopic(SocketChannel clientChannel, String topic) {
		topicSubscribers.putIfAbsent(topic, new HashSet<>());
		topicSubscribers.get(topic).add(clientChannel);

		clientSubscriptions.putIfAbsent(clientChannel, new HashSet<>());
		clientSubscriptions.get(clientChannel).add(topic);
	}

	private void unsubscribeClientFromTopic(SocketChannel clientChannel, String topic) {
		if (topicSubscribers.containsKey(topic)) {
			topicSubscribers.get(topic).remove(clientChannel);
			if (topicSubscribers.get(topic).isEmpty()) {
				topicSubscribers.remove(topic);
			}
		}

		if (clientSubscriptions.containsKey(clientChannel)) {
			clientSubscriptions.get(clientChannel).remove(topic);
			if (clientSubscriptions.get(clientChannel).isEmpty()) {
				clientSubscriptions.remove(clientChannel);
			}
		}
	}

	private void sendMessageToTopic(String topic, String message) throws IOException {
		if (topicSubscribers.containsKey(topic)) {
			ByteBuffer msgBuffer = ByteBuffer.wrap(message.getBytes());
			for (SocketChannel subscriber : topicSubscribers.get(topic)) {
				subscriber.write(msgBuffer);
				msgBuffer.reset(); // Reset buffer for next write
			}
		}
	}
}