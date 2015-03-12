package chat.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

class ServerUser extends Thread{
	
	private AsynchronousSocketChannel clientChannel;
	private String nickName;
	private boolean online;
	private Future<Integer> result;
	private ByteBuffer buffer; 
	
	/**
	 * @param clientChannel
	 */
	public ServerUser(AsynchronousSocketChannel clientChannel) {
		this.clientChannel = clientChannel;
		this.online = true;
		this.buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
		this.result = clientChannel.read(this.buffer);
	}
	
	public AsynchronousSocketChannel getClientChannel(){
		return this.clientChannel;
	}
	
	public String getNickName(){
		return this.nickName;
	}
	
	public void setNickName(String nick){
		this.nickName=nick;
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public Future<Integer> getResult() {
		return result;
	}

	public void setResult(Future<Integer> result) {
		this.result = result;
	}

	public void closeUserChannel(){
		try {
			this.clientChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isOnline(){
		return online;
	}
	
}
