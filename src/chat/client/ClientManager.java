package chat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClientManager {

	private static AsynchronousSocketChannel client;
	private static ConsoleListener console = ConsoleListener.getInstance();
	private static ClientListener listener;
	private static String command = "";
	
	public static AsynchronousSocketChannel getClient(){
		return client;
	}
	/** Connects to the chat server
	 * @param host_Adress
	 * @param listening_port
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	static void makeConnection(String host_Adress,int listening_port) throws IOException, InterruptedException, ExecutionException {
		
		client = AsynchronousSocketChannel.open();
		InetSocketAddress hostAddress = new InetSocketAddress(host_Adress, listening_port);
		Future<Void> future = client.connect(hostAddress);
		future.get(); // waits until is connected
		System.out.println("Client is started: " + client.isOpen());
		byte [] byteMessage = Constants.NICK_NAME.getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(byteMessage);
		Future<Integer> writeResult = client.write(buffer);
		while (!writeResult.isDone()) {}	//waits until NICK_NAME is sent to the server	
	}
	
	public static void main(String[] args) {
		
		try {
			makeConnection(Constants.HOST_ADRESS, Constants.LISTENING_PORT);
		} catch (IOException | InterruptedException | ExecutionException e) {
			System.out.println("Unable to connect to server, please type " + Constants.START_COMMAND + " to connect");
		}
		console.start();
		listener = new ClientListener();
		listener.start();
		while (!command.equals(Constants.EXIT_COMMAND)){
			command = ConsoleListener.getCommand();
			if (command.equals(Constants.QUIT_SERVER_COMMAND)) {
				try {
					ClientManager.client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ConsoleListener.setCommand("");
			}
			if (command.equals(Constants.START_COMMAND)) {
				listener = new ClientListener();
				listener.start();
				System.out.println("starting listener");
				ConsoleListener.setCommand("");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		console.interrupt();
		listener.interrupt();
		System.out.println("goodbye");
		}
}
