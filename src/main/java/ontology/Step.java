package ontology;

import java.util.Set;

public class Step {

	private int stepNr;
	private LogInfo info = null;
	private Set<MessageEdge> addedMessages = null;
	private Set<MessageEdge> removedMessages = null;
	private MessageEdge receivedMessage = null;
	private Set<Coalition> addedCoalitions = null;
	private Set<Coalition> removedCoalitions = null;
	private Set<String> newKnownAgents = null;
	private String agent = null;

	public Step(int stepNr, LogInfo info, Set<MessageEdge> addedMessages, Set<MessageEdge> removedMessages,
			MessageEdge receivedMessage, Set<Coalition> addedCoalitions, Set<Coalition> removedCoalitions,
			Set<String> newKnownAgents, String agent) {
		this.stepNr = stepNr;
		this.info = info;
		this.addedMessages = addedMessages;
		this.removedMessages = removedMessages;
		this.receivedMessage = receivedMessage;
		this.addedCoalitions = addedCoalitions;
		this.removedCoalitions = removedCoalitions;
		this.newKnownAgents = newKnownAgents;
		this.agent = agent;
	}

	public Set<Coalition> getAddedCoalitions() {
		return addedCoalitions;
	}

	public Set<MessageEdge> getAddedMessages() {
		return addedMessages;
	}

	public String getAgent() {
		return agent;
	}

	public LogInfo getInfo() {
		return info;
	}

	public Set<String> getNewKnownAgents() {
		return newKnownAgents;
	}

	public MessageEdge getReceivedMessage() {
		return receivedMessage;
	}

	public Set<Coalition> getRemovedCoalitions() {
		return removedCoalitions;
	}

	public Set<MessageEdge> getRemovedMessages() {
		return removedMessages;
	}

	public int getStepNr() {
		return stepNr;
	}

	public boolean receivedMessage() {
		return receivedMessage != null;
	}

	public boolean sentMessage() {
		return addedMessages != null && !addedMessages.isEmpty();
	}

	public void setAddedCoalitions(Set<Coalition> addedCoalitions) {
		this.addedCoalitions = addedCoalitions;
	}

	public void setInfo(LogInfo info) {
		this.info = info;
	}

	public void setNewKnownAgents(Set<String> newKnownAgents) {
		this.newKnownAgents = newKnownAgents;
	}

	public void setRemovedCoalitions(Set<Coalition> removedCoalitions) {
		this.removedCoalitions = removedCoalitions;
	}

	public void setStepNr(int stepNr) {
		this.stepNr = stepNr;
	}
}
