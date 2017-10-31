package ontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class World {

	private ProtocolPhase protocolPhase = null;
	private boolean inviter = false;
	private boolean ready = false;
	private int missingReplies;
	protected Message currentInvitation = null;
	protected Map<UUID, Message> receivedInvitations = null;
	protected Map<String, Double> receivedDemands = null;
	protected Set<UUID> unsuccessfulNegotiations = null;
	protected LinkedList<Coalition> preferredCoalitions = null;
	protected HashMap<String, Double> negotiatedPayoffs = null;
	protected List<Log> logs = null;

	private Agent self = null;
	private Coalition singleCoalition = null;
	private LinkedList<Coalition> coalitions = null;

	public World() {
		self = new Agent();
		coalitions = new LinkedList<Coalition>();
		receivedInvitations = new HashMap<UUID, Message>();
		receivedDemands = new HashMap<String, Double>();
		negotiatedPayoffs = new HashMap<String, Double>();
		unsuccessfulNegotiations = new HashSet<UUID>();
		/* coalitions with value > singleCoalitionValue */
		preferredCoalitions = new LinkedList<Coalition>();
		logs = new LinkedList<Log>();
	}

	public void addAgent(String agent) {
		self.getKnownAgents().add(agent);
	}

	public void addAgents(Collection<String> agents) {
		self.getKnownAgents().addAll(agents);
	}

	public void addCoalition(Coalition coalition) {
		coalitions.add(coalition);
	}

	public void decreaseMissingReplies() {
		missingReplies--;
	}

	public Coalition getCoalition(Set<String> agents) {
		for (Coalition coalition : coalitions) {
			if (coalition.getAgents().equals(agents)) {
				return coalition;
			}
		}
		return null;
	}

	public LinkedList<Coalition> getCoalitions() {
		return coalitions;
	}

	public Message getCurrentInvitation() {
		return currentInvitation;
	}

	public Set<String> getKnownAgents() {
		return self.getKnownAgents();
	}

	public List<Log> getLogs() {
		return logs;
	}

	public int getMissingReplies() {
		return missingReplies;
	}

	public Coalition getMyCoalition() {
		return self.getCoalition();
	}

	public String getMyName() {
		return self.getName();
	}

	public double getMyPayoff() {
		return self.getPayoff();
	}

	public HashMap<String, Double> getNegotiatedPayoffs() {
		return negotiatedPayoffs;
	}

	public LinkedList<Coalition> getPreferredCoalitions() {
		return preferredCoalitions;
	}

	public Map<String, Double> getReceivedDemands() {
		return receivedDemands;
	}

	public Map<UUID, Message> getReceivedInvitations() {
		return receivedInvitations;
	}

	public Agent getSelf() {
		return self;
	}

	public Coalition getSingleCoalition() {
		return singleCoalition;
	}

	public double getSingleCoalitionPayoff() {
		return singleCoalition.getAgentPayoff(getMyName());
	}

	public Set<UUID> getUnsuccessfulNegotiations() {
		return unsuccessfulNegotiations;
	}

	public boolean isInInvitationPhase() {
		return protocolPhase == ProtocolPhase.INVITATION;
	}

	public boolean isInKnowledgeExchangePhase() {
		return protocolPhase == ProtocolPhase.KNOWLEDGE_EXCHANGE;
	}

	public boolean isInNegotiationPhase() {
		return isInInvitationPhase() || isInKnowledgeExchangePhase();
	}

	public boolean isInviter() {
		return inviter;
	}

	public boolean isReady() {
		return ready;
	}

	public void logReceivedMessage(Message message) {

		switch (message.getInfo()) {
		case CONFIRMING_MY_PARTICIPATION:
			logs.add(new Log(LogInfo.RECEIVED_CONFIRMING_MY_PARTICIPATION, getMyName(), message));
			break;

		case CONFIRM_YOUR_PARTICIPATION:
			logs.add(new Log(LogInfo.RECEIVED_CONFIRM_YOUR_PARTICIPATION, getMyName(), message));
			break;

		case LEAVING_COALITION:
			logs.add(new Log(LogInfo.RECEIVED_LEAVING_COALITION, getMyName(), message));
			break;

		case CANCELLING_NEGOTIATION:
			logs.add(new Log(LogInfo.RECEIVED_CANCELLING_NEGOTIATION, getMyName(), message));
			break;

		case DEMAND:
			logs.add(new Log(LogInfo.RECEIVED_DEMAND, getMyName(), message));
			break;

		case INVITATION_REJECTED:
			logs.add(new Log(LogInfo.RECEIVED_INVITATION_REJECTED, getMyName(), message));
			break;

		case NEGOTIATION_UNSUCCESSFUL:
			logs.add(new Log(LogInfo.RECEIVED_NEGOTIATION_UNSUCCESSFUL, getMyName(), message));
			break;

		case NEGOTIATION_CANCELLED:
			logs.add(new Log(LogInfo.RECEIVED_NEGOTIATION_CANCELLED, getMyName(), message));
			break;

		case INVITATION:
			logs.add(new Log(LogInfo.RECEIVED_INVITATION, getMyName(), message));
			break;

		case KNOWN_AGENTS:
			logs.add(new Log(LogInfo.RECEIVED_KNOWN_AGENTS, getMyName(), message));
			break;

		case AGENTS_KNOWN_TO_COALITION:
			logs.add(new Log(LogInfo.RECEIVED_AGENTS_KNOWN_TO_COALITION, getMyName(), message));
			break;

		case REJECTING_INVITATION:
			logs.add(new Log(LogInfo.RECEIVED_REJECTING_INVITATION, getMyName(), message));
			break;

		case NEW_COALITION:
			logs.add(new Log(LogInfo.RECEIVED_NEW_COALITION, getMyName(), message));
			break;
		}
	}

	public void logSentMessage(Message message) {

		switch (message.getInfo()) {
		case CONFIRMING_MY_PARTICIPATION:
			logs.add(new Log(LogInfo.SENT_CONFIRMING_MY_PARTICIPATION, getMyName(), message));
			break;

		case CONFIRM_YOUR_PARTICIPATION:
			logs.add(new Log(LogInfo.SENT_CONFIRM_YOUR_PARTICIPATION, getMyName(), message));
			break;

		case LEAVING_COALITION:
			logs.add(new Log(LogInfo.SENT_LEAVING_COALITION, getMyName(), message));
			break;

		case CANCELLING_NEGOTIATION:
			logs.add(new Log(LogInfo.SENT_CANCELLING_NEGOTIATION, getMyName(), message));
			break;

		case NEGOTIATION_CANCELLED:
			logs.add(new Log(LogInfo.SENT_NEGOTIATION_CANCELLED, getMyName(), message));
			break;

		case DEMAND:
			logs.add(new Log(LogInfo.SENT_DEMAND, getMyName(), message));
			break;

		case INVITATION_REJECTED:
			logs.add(new Log(LogInfo.SENT_INVITATION_REJECTED, getMyName(), message));
			break;

		case NEGOTIATION_UNSUCCESSFUL:
			logs.add(new Log(LogInfo.SENT_NEGOTIATION_UNSUCCESSFUL, getMyName(), message));
			break;

		case INVITATION:
			logs.add(new Log(LogInfo.SENT_INVITATION, getMyName(), message));
			break;

		case KNOWN_AGENTS:
			logs.add(new Log(LogInfo.SENT_KNOWN_AGENTS, getMyName(), message));
			break;

		case AGENTS_KNOWN_TO_COALITION:
			logs.add(new Log(LogInfo.SENT_KNOWN_AGENTS_BY_COALITION, getMyName(), message));
			break;

		case REJECTING_INVITATION:
			logs.add(new Log(LogInfo.SENT_REJECTING_INVITATION, getMyName(), message));
			break;

		case NEW_COALITION:
			logs.add(new Log(LogInfo.SENT_NEW_COALITION, getMyName(), message));
			break;
		}
	}

	public void setCoalitions(LinkedList<Coalition> coalitions) {
		this.coalitions = coalitions;
	}

	public void setCurrentInvitation(Message currentInvitation) {
		this.currentInvitation = currentInvitation;
	}

	public void setInviter(boolean inviter) {
		this.inviter = inviter;
	}

	public void setMissingReplies(int missingReplies) {
		this.missingReplies = missingReplies;
	}

	public void setMyCoalition(Coalition coalition) {
		self.setCoalition(coalition);
		if (coalition.getPayoffs() != null && !coalition.getPayoffs().isEmpty()) {
			setMyPayoff(coalition.getAgentPayoff(getMyName()));
		}
	}

	public void setMyCoalition(Set<String> agents) {
		setMyCoalition(getCoalition(agents));
	}

	public void setMyName(String name) {
		self.setName(name);
	}

	public void setMyPayoff(double payoff) {
		self.setPayoff(payoff);
	}

	public void setNegotiatedPayoffs(HashMap<String, Double> negotiatedPayoffs) {
		this.negotiatedPayoffs = negotiatedPayoffs;
	}

	public void setPreferredCoalitions(LinkedList<Coalition> preferredCoalitions) {
		this.preferredCoalitions = preferredCoalitions;
	}

	public void setProtocolPhase(ProtocolPhase protocolPhase) {
		this.protocolPhase = protocolPhase;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void setReceivedDemands(Map<String, Double> receivedDemands) {
		this.receivedDemands = receivedDemands;
	}

	public void setReceivedInvitations(Map<UUID, Message> receivedInvitations) {
		this.receivedInvitations = receivedInvitations;
	}

	public void setSingleCoalition(Coalition singleCoalition) {
		this.singleCoalition = singleCoalition;
	}
}
