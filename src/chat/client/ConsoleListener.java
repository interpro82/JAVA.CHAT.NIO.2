package chat.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ConsoleListener extends Thread {

	private static ConsoleListener instance;
	private static String command = "";
		
	private ConsoleListener(){}
	
	public static ConsoleListener getInstance(){
		if (instance == null){
			instance = new ConsoleListener();
		}
		return instance;
	}
	
	static synchronized String getCommand(){
		return command;
	}
	
	static synchronized void setCommand(String newCommand){
		command = newCommand;
	}
	
	public static byte[] serialize(Message msg) throws IOException {
	    Object obj = msg;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}
	
	/**
	 * @param message
	 * @param msgReciever
	 */
	void sendMessage(String message, String msgReciever){
		if (!ClientManager.getClient().isOpen()) {
			System.out.println("channel is not open");
			return;
		}
		byte[] byteMessage = null;
		try {
			Message myMessage = new Message(Constants.NICK_NAME, message, msgReciever);  
			byteMessage = serialize(myMessage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ByteBuffer buffer = ByteBuffer.wrap(byteMessage);
		System.out.println(buffer.capacity());
		Future<Integer> result = ClientManager.getClient().write(buffer);
		try {
			if ( result.get().intValue() == -1 ){
				System.out.println("closing connection");
				ClientManager.getClient().close();
				result.cancel(true);
				buffer.clear();
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sent message to server: " + message);
		buffer.clear();
	} 
	
	@Override
	public void run() {
		String message = "";
		Scanner sc = new Scanner(System.in);
		while (!message.equals(Constants.EXIT_COMMAND) & !isInterrupted()) {
			message = sc.nextLine();
			sendMessage(message, Constants.RECIEVER);
			if (message.equals(Constants.QUIT_SERVER_COMMAND)){
				command = Constants.QUIT_SERVER_COMMAND;
			}
			if (message.equals(Constants.START_COMMAND) & !ClientManager.getClient().isOpen()){
				try {
					ClientManager.makeConnection(Constants.HOST_ADRESS, Constants.LISTENING_PORT);
					command = Constants.START_COMMAND;
				} catch (IOException | InterruptedException | ExecutionException e) {
					System.out.println("Unable to connect to server");
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("shutting down..");
		sendMessage(Constants.QUIT_SERVER_COMMAND, Constants.RECIEVER);
		sc.close();
		try {
			ClientManager.getClient().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		command = Constants.EXIT_COMMAND;
		System.out.println("console is stopped");
	}
}
