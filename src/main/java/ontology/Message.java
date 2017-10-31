package ontology;

import java.util.Set;
import java.util.UUID;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Message implements IFact {

	private static final long serialVersionUID = -8342954137697718436L;

	private Object content = null;
	private MessageInfo info = null;
	private UUID negotiationID = null;
	private Set<String> receivers = null;
	private String sender = null;
	private long timeStamp;

	public Message(Message message) {
		this.info = message.getInfo();
		this.negotiationID = message.getNegotiationID();
		this.sender = message.getSender();
		this.receivers = message.getReceivers();
		this.content = message.getContent();
		this.timeStamp = message.getTimeStamp();
	}

	public Message(MessageInfo info) {
		super();
		this.info = info;
	}

	public Message(MessageInfo info, UUID negotiationID, String sender, Set<String> receivers) {
		this.info = info;
		this.negotiationID = negotiationID;
		this.sender = sender;
		this.receivers = receivers;
		this.timeStamp = System.currentTimeMillis();
	}

	public Message(MessageInfo info, UUID negotiationID, String sender, Set<String> receivers, Object content) {
		this.info = info;
		this.negotiationID = negotiationID;
		this.sender = sender;
		this.receivers = receivers;
		this.content = content;
		this.timeStamp = System.currentTimeMillis();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Message))
			return false;
		Message other = (Message) obj;
		if (info != other.info)
			return false;
		if (negotiationID == null) {
			if (other.negotiationID != null)
				return false;
		} else if (!negotiationID.equals(other.negotiationID))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		return true;
	}

	public Object getContent() {
		return content;
	}

	public MessageInfo getInfo() {
		return info;
	}

	public UUID getNegotiationID() {
		return negotiationID;
	}

	public Set<String> getReceivers() {
		return receivers;
	}

	public String getSender() {
		return sender;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + ((negotiationID == null) ? 0 : negotiationID.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		return result;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public void setInfo(MessageInfo info) {
		this.info = info;
	}

	public void setReceivers(Set<String> receivers) {
		this.receivers = receivers;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
}
