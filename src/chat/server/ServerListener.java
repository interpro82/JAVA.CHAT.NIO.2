package chat.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Iliya Nikolov
 * The class is started form the ServerManager.
 * It listens for new user connections to the server and adds them to the Server's user list.
 */
public class ServerListener extends Thread {
	
	private String host_Adress;
	private int port;
	private static ServerListener instance = null;
	private AsynchronousServerSocketChannel serverChannel;
	
	private ServerListener(String host_Adress, int port) throws IOException{
		this.host_Adress = host_Adress;
		this.port = port;
		this.serverChannel = null;
		openPort(this.host_Adress, this.port);
	}
	
	/**
	 * @param host_Adress
	 * @param port
	 * @return
	 */
	public static ServerListener getInstance(String host_Adress, int port){
		if (instance == null) {
			try {
				instance = new ServerListener(host_Adress, port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		return instance;
	}
		
	private void openPort(String host_Adress, int listening_Port) throws IOException{ //creates and opens a channel
		this.serverChannel = AsynchronousServerSocketChannel.open();  
		InetSocketAddress hostAddress = new InetSocketAddress(host_Adress, listening_Port);
		this.serverChannel.bind(hostAddress);
		System.out.println("Server channel bound to port: " + hostAddress.getPort());
		System.out.println("Waiting for client to connect... ");
	}
	
	private ServerUser getUser(AsynchronousServerSocketChannel serverChannel) throws InterruptedException, ExecutionException{
		Future<AsynchronousSocketChannel> acceptResult = serverChannel.accept();  //waits until connection is established
		AsynchronousSocketChannel clientChannel = acceptResult.get(); // creates new socket for client's service
		ServerUser newUser = new ServerUser(clientChannel); //creates new user that is connected to the server
		while (!newUser.getResult().isDone()) {
			Thread.sleep(100);//waits for the nickName to arrive 
		}	
		newUser.getBuffer().flip();
		String userName = new String(newUser.getBuffer().array()).trim();
		System.out.println(userName + " is connected");
		newUser.setNickName(userName);
		newUser.getBuffer().clear();
		newUser.setBuffer(ByteBuffer.allocate(Constants.BUFFER_SIZE)); 
		newUser.setResult(newUser.getClientChannel().read(newUser.getBuffer()));
		return newUser;
	}
	
	void stopServer(){ //not used yet and not tested
		try {
			synchronized (ServerManager.userList) {
				for (ServerUser user : ServerManager.userList){
					user.closeUserChannel();
					ServerManager.userList.remove(user);
				}
			}
			this.serverChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (!isInterrupted()) {
			try {
				ServerUser newUserToAdd = getUser(this.serverChannel);
				ServerManager.userList.add(newUserToAdd);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
}
