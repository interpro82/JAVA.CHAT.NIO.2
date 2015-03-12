package chat.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class ClientListener extends Thread {
	
	ClientListener(){}
	
	@Override
	public void run(){
	
		System.out.println("listener started");
		while( ClientManager.getClient().isOpen() & !isInterrupted()){
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				Future<Integer> readResult = ClientManager.getClient().read(readBuffer);
				while (readResult.isDone()){ //waits until message has arrived or connection is lost 
					if (!ClientManager.getClient().isOpen()) {
						try {
							Thread.sleep(100); //wait to check if the channel is not temporary closed
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {		// we have result		
					if (readResult.get().intValue() == -1) {
						System.out.println("closing connection");
						ClientManager.getClient().close();
					} else {			
						readBuffer.flip();
						if (readBuffer.hasArray()) {
							String newMessage = new String(readBuffer.array()).trim();
							readBuffer.clear();
							System.out.println(newMessage);
						}
					}
				} catch (InterruptedException | IOException e) {
						e.printStackTrace();
				} catch (ExecutionException e) {
					System.out.println("connection closed");
					if ( ClientManager.getClient().isOpen())
						try {
							ClientManager.getClient().close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}	
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		System.out.println("listener is stopped");
	}
}
