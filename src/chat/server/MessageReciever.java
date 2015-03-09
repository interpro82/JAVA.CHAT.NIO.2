package chat.server;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import chat.client.Message;
/**
 * @author Iliya Nikolov
 * The class is started form the MessageManager.
 * It listens for new messages from the connected users via only one thread for all users.
 * Uses ServerManager's userList to know what users are connected.
 * adds received messages to MessageManager's message list.
 */
public class MessageReciever extends Thread {
	
	private static MessageReciever instance = null;
	
	public static MessageReciever getInstance(){
		if (instance == null){
			instance = new MessageReciever();
		}
		return instance;
	}
	
	private Message deserialize(byte[] data) throws IOException, ClassNotFoundException { 
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    Object obj = is.readObject();
	    Message message = (Message) obj;
	    return message; 
	}
	
	private Message getUserMessage() {
		String userMessage = null;
		Message myUserMessage = null;
		if (!ServerManager.userList.isEmpty()) {
			ServerUser[] newlist = new ServerUser[ServerManager.userList.size()];
			ServerManager.userList.copyInto(newlist); 
			synchronized(newlist) {
				for (ServerUser user : newlist) {
					try {
						if (!user.getClientChannel().isOpen()) {
							return null;
						}
						if (user.getResult().isDone()) {
							if (user.getResult().get().intValue() == -1) {
								System.out.println("closing connection");
								userMessage = Constants.QUIT_COMMAND; 
								user.getClientChannel().close();
								user.getResult().cancel(true);
								user.getBuffer().clear(); 
							} else {
								user.getBuffer().flip();
								if (user.getBuffer().hasArray()) {
									myUserMessage =  deserialize(user.getBuffer().array());
									userMessage = myUserMessage.getText();
									user.getBuffer().clear();
									user.setBuffer(ByteBuffer.allocate(Constants.BUFFER_SIZE));
									user.setResult(user.getClientChannel().read(user.getBuffer()));
								}
							}
						} 
					} catch (ExecutionException e) {
						System.out.println("Execution exception-closing user chanel");
						user.getResult().cancel(true);
						user.closeUserChannel();
						ServerManager.userList.remove(user);
					} catch (IOException ioe) {
						System.out.println("Interrupted exception-closing user chanel");
						ioe.printStackTrace();
						user.getResult().cancel(true);
						user.closeUserChannel();
						ServerManager.userList.remove(user);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if (userMessage != null){
						if (userMessage.equals(Constants.QUIT_COMMAND)) {
							System.out.println("user disconnected");
							ServerManager.userList.remove(user);
						}
						return myUserMessage;
					}
				}
			}
		}
		return myUserMessage;
	}
	
	public void run(){
		while(!isInterrupted()){
			Message newMessage = getUserMessage();  //TODO make it with wait-notify 
			if (newMessage != null) {
				System.out.println(newMessage.getText());
				MessageManager.addMessageToList(newMessage);;		
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
