package chat.server;

import java.util.Vector;

/**
 * @author Iliya Nikolov
 * This class starts the application -"ChatServer" 
 * keeps list of Users that are connected to the server.
 */
public class ServerManager {

	public static Vector<ServerUser> userList = new Vector<ServerUser>();
	
	public static void main(String[] args) {
		ServerListener serverListener = ServerListener.getInstance(Constants.HOST_ADRESS,Constants.LISTENING_PORT );
		MessageManager messageManager = MessageManager.getInstance();
		serverListener.start();
		messageManager.start();
		try {
			serverListener.join();
			messageManager.join();
		} catch (InterruptedException e) {
				e.printStackTrace();
		}
	}
}
