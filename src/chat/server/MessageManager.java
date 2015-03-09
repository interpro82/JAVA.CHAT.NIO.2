package chat.server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import chat.client.Message;

public class MessageManager extends Thread {
	
	private static MessageManager instance = null;
	private static Vector<Message> messageList = null;
	
	private MessageManager(){
		messageList = new Vector<Message>();
	}
	
	static MessageManager getInstance(){
		if (instance == null) {
			instance = new MessageManager();
		} 
		return instance;	
	}
	
	public static synchronized int getMessageListSize() {
		return messageList.size();
	}
	
	public static synchronized Message getMessageAt(int index) {
		return messageList.get(index);
	}
	
	public static synchronized void addMessageToList(Message message){
		messageList.addElement(message);
	}
	
	public static synchronized void removeMessageAt(int index){
		messageList.remove(index);
	}
	
	public static synchronized Message[] copyMessageListToArray(){
		Message[] newList = new Message[getMessageListSize()];
		messageList.copyInto(newList);
		return newList;
	}
	
	private static void sendMessageToUser(AsynchronousSocketChannel client, String message){
		if (!client.isOpen()) {
			return;
		}
		byte [] byteMessage = message.getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(byteMessage);
		Future<Integer> result = client.write(buffer);
		try {
			if (result.get().intValue() == -1){
				System.out.println("closing connection");
				client.close();
				result.cancel(true);
				buffer.clear();
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sent message to user: " + message);
		buffer.clear();
	}
	
	
	private static void sendAllMessages(){
		ServerUser[] tempUserList = new ServerUser[ServerManager.userList.size()];
		ServerManager.userList.copyInto(tempUserList); 
		Message[] tempMessageList = copyMessageListToArray();
		synchronized(tempMessageList){ //TODO : remove synchronized
			int index = 0;
			for (Message message : tempMessageList) {
				synchronized(tempUserList){
					for (ServerUser user : tempUserList){
						if (user.getNickName().equals(message.getReciever())){
							String sender = message.getSender() + " wrote: ";
							sendMessageToUser(user.getClientChannel(), sender);
							String textMessage = message.getText();
							sendMessageToUser(user.getClientChannel(), textMessage);
						}
					}
					removeMessageAt(index);
					index++;
				}
			}
			
		}
		
	}
	
	public void run(){
		MessageReciever messageReciever = MessageReciever.getInstance();
		messageReciever.start();
		while (!isInterrupted()){
			if (!messageList.isEmpty()){ // TODO : make it with wait and notify - producer/client
				sendAllMessages();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
}
