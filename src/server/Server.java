package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.CommandType;

public class Server {
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private List<String> topics;
	private Map<String, Set<SocketChannel>> topicSubscribers;
	private Map<SocketChannel, Set<String>> clientSubscriptions;

	public Server(int port) throws IOException {
		this.selector = Selector.open();
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.configureBlocking(false);
		this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.topics = new ArrayList<>();
		this.topicSubscribers = new HashMap<>();
		this.clientSubscriptions = new HashMap<>();
	}


	public static void main(String[] args) {
		try {
			Server server = new Server(12345);
			server.start();
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
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
			e.printStackTrace();
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

	private void processCommand(SocketChannel clientChannel, String request) throws IOException {

		String[] parts = request.split(" ", 2);
		String command = parts[0];
		String argument = parts.length > 1 ? parts[1] : "";

		switch (CommandType.valueOf(command.toUpperCase())) {
			case ADD_TOPIC -> addTopic(argument);
			case REMOVE_TOPIC -> removeTopic(argument);
			case SUBSCRIBE -> subscribeClientToTopic(clientChannel, argument);
			case UNSUBSCRIBE -> unsubscribeClientFromTopic(clientChannel, argument);
			case SEND_MESSAGE -> sendMessageToTopic(command, argument);
			case REFRESH -> sendTopicList(clientChannel);
			default -> System.out.println("Unknown command: " + command);
		}
	}

	private void addTopic(String topic) {
		if (!topics.contains(topic)) {
			topics.add(topic);
			topicSubscribers.put(topic, new HashSet<>());
			System.out.println("Added topic: " + topic);
		} else {
			System.out.println("Topic already exists: " + topic);
		}
	}

	private void removeTopic(String topic) {
		if (!topics.contains(topic)) {
			System.out.println("Topic does not exist: " + topic);
			return;
		}
			topics.remove(topic);
			topicSubscribers.remove(topic);
			for (Set<String> subscriptions : clientSubscriptions.values()) {
				subscriptions.remove(topic);
			}
			System.out.println("Removed topic: " + topic);
	}

	private void subscribeClientToTopic(SocketChannel clientChannel, String topic) {
		if (!topics.contains(topic)) {
			System.out.println("Topic does not exist: " + topic);
			return;
		}
		topicSubscribers.get(topic).add(clientChannel);
		clientSubscriptions.get(clientChannel).add(topic);
	}

	private void unsubscribeClientFromTopic(SocketChannel clientChannel, String topic) {
		if (!topics.contains(topic)) {
			System.out.println("Topic does not exist: " + topic);
			return;
		}
		if (topics.contains(topic) && topicSubscribers.containsKey(topic)) {
			topicSubscribers.get(topic).remove(clientChannel);
			clientSubscriptions.get(clientChannel).remove(topic);
		}
	}

	private void sendTopicList(SocketChannel clientChannel) throws IOException {
		String topicsList = String.join(",", topics);
		ByteBuffer buffer = ByteBuffer.wrap(topicsList.getBytes());
		clientChannel.write(buffer);
		buffer.clear();
	}

	private void sendMessageToTopic(String topic, String message) throws IOException {
		if (!topics.contains(topic)) {
			System.out.println("Topic does not exist: " + topic);
			return;
		}
		if (topicSubscribers.containsKey(topic)) {
			ByteBuffer msgBuffer = ByteBuffer.wrap(message.getBytes());
			for (SocketChannel subscriber : topicSubscribers.get(topic)) {
				subscriber.write(msgBuffer);
				msgBuffer.clear(); // Reset buffer for next write
			}
		}
	}
}