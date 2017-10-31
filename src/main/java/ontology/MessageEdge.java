package ontology;

public class MessageEdge extends Message {

	private static final long serialVersionUID = 8191279529506114781L;

	private String from;
	private String to;
	private boolean received;

	public MessageEdge(Message message, String from, String to) {
		super(message);
		this.from = from;
		this.to = to;
	}

	public boolean equals(Message message) {
		return super.equals(message);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof MessageEdge))
			return false;
		MessageEdge other = (MessageEdge) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}
}
