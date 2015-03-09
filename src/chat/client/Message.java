package chat.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable{

	private String sender;
	private String text;
	private String reciever;
	
	/**
	 * @param sender
	 * @param text
	 * @param reciever
	 */
	public Message(String sender, String text, String reciever) {
		super();
		this.sender = sender;
		this.text = text;
		this.reciever = reciever;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getReciever() {
		return reciever;
	}

	public void setReciever(String reciever) {
		this.reciever = reciever;
	}
	
	
}
