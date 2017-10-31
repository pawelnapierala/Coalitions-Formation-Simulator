package beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ontology.Coalition;
import ontology.Log;
import ontology.Message;
import ontology.MessageInfo;
import ontology.ProtocolPhase;
import ontology.World;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import simulator.SimulatorModel;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class ProtocolBean extends AbstractAgentBean {

	private class MessageObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = 1474729664709853016L;

		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if (event instanceof WriteCallEvent<?>) {

				Message message = (Message) (memory.remove(((WriteCallEvent<IJiacMessage>) event).getObject()))
						.getPayload();

				world.logReceivedMessage(message);
				log.info(world.getMyName() + ": received " + message.getInfo() + " {" + message.getContent() + "} from "
						+ message.getSender());

				switch (message.getInfo()) {

				case INVITATION:
					log.info(world.getMyName() + ": received INVITATION " + message.getContent() + " from "
							+ message.getSender());
					receivedInvitation(message);
					break;

				case DEMAND:

					receivedDemand(message);
					break;

				case CONFIRM_YOUR_PARTICIPATION:

					receivedConfirmYourParticipation(message);
					break;

				case CONFIRMING_MY_PARTICIPATION:

					receivedConfirmingMyParticipation(message.getNegotiationID(), message.getSender());
					break;

				case REJECTING_INVITATION:

					receivedRejectingInvitation(message.getNegotiationID());
					break;

				case CANCELLING_NEGOTIATION:

					receivedCancellingNegotiation(message.getNegotiationID(), message.getSender());
					break;

				case NEGOTIATION_CANCELLED:
					receivedNegotiationCancelled(message.getNegotiationID());
					break;

				case NEW_COALITION:
					receivedNewCoalition(message.getSender(), (Map<String, Double>) message.getContent());
					break;

				case INVITATION_REJECTED:
					receivedInvitationRejected(message.getNegotiationID());
					break;

				case NEGOTIATION_UNSUCCESSFUL:
					receivedNegotiationUnsuccessful(message.getNegotiationID());
					break;

				case LEAVING_COALITION:
					receivedLeavingCoalition((Set<String>) message.getContent());
					break;

				case KNOWN_AGENTS:
					receivedKnownAgents((Set<String>) message.getContent());
					break;

				case AGENTS_KNOWN_TO_COALITION:
					receivedKnownAgents((Set<String>) message.getContent());
					break;

				default:
					log.error(world.getMyName() + ": message not recognized!");
				}
			}
		}
	}

	private StrategyBean playerBean = null;

	private World world = null;

	private Action sendAction = null;

	SimulatorModel simulatorModel = null;

	private void analyseDemands() {

		HashMap<String, Double> payoffs = playerBean.analyseDemands(); // returns null if cannot fulfill all demands

		if (payoffs == null) {
			// negotiation unsuccessful

			log.info(world.getMyName() + ": cannot fulfill all demands, negotiation unsuccessful");

			sendNegotiationUnsuccessful();

			world.getPreferredCoalitions().remove(
					world.getCoalition((Set<String>) world.getCurrentInvitation().getContent()));
			world.setCurrentInvitation(null);
			world.setInviter(false);
			world.setProtocolPhase(null);
			begin();

		} else {
			// negotiation successful
			log.info(world.getMyName() + ": can fulfill all demands, payoffs = " + payoffs);

			world.setProtocolPhase(ProtocolPhase.KNOWLEDGE_EXCHANGE);
			world.setNegotiatedPayoffs(payoffs);
			world.setMissingReplies(payoffs.size() - 1);

			sendConfirmYourParticipation();
		}
	}

	private void begin() {

		if (!world.isInNegotiationPhase()) {

			if (world.isReady()) {
				simulatorModel.setAgentReady(world.getMyName(), false);
			}

			Message oldestInvitation = null;

			while (!world.getReceivedInvitations().isEmpty()) {
				// answer to invitation

				oldestInvitation = getOldestInvitation();

				if (playerBean.isInvitationAcceptable(oldestInvitation)) {
					// answer to invitation

					world.setProtocolPhase(ProtocolPhase.INVITATION);
					world.setCurrentInvitation(oldestInvitation);
					double myDemand = playerBean.getMyDemand(world.getCoalition((Set<String>) oldestInvitation
							.getContent()));

					sendDemand(myDemand);
					return;

				} else {
					// reject inv

					world.getReceivedInvitations().remove(oldestInvitation.getNegotiationID());
					sendRejectingInvitation(oldestInvitation);
				}
			}

			Coalition preferredCoalition = playerBean.getPreferredCoalition();

			if (preferredCoalition == null) {
				// == null, stay in current coalition, ready

				log.info(world.getMyName() + ": READY, my coalition is " + world.getMyCoalition().getPayoffs());
				world.setReady(true);
				simulatorModel.setAgentReady(world.getMyName(), true);

			} else {
				if (!world.isInNegotiationPhase()) {

					if (preferredCoalition.getSize() == 1) {
						// work alone, leave current coalition

						log.info(world.getMyName() + ": my best option is to work alone, leaving my current coalition "
								+ world.getMyCoalition().getAgents());

						sendLeavingCoalition(stringToSet(world.getMyName()), world.getMyCoalition().getAgents());
						world.setMyCoalition(preferredCoalition);

						log.info(world.getMyName() + ": READY");
						world.setReady(true);
						simulatorModel.setAgentReady(world.getMyName(), true);

					} else {
						// send invitation

						world.setProtocolPhase(ProtocolPhase.INVITATION);
						world.setInviter(true);
						world.setCurrentInvitation(new Message(MessageInfo.INVITATION, UUID.randomUUID(), world
								.getMyName(), preferredCoalition.getAgents(), preferredCoalition.getAgents()));
						world.setMissingReplies(((Set<String>) world.getCurrentInvitation().getContent()).size() - 1);
						world.getReceivedDemands().clear();

						sendInvitation(world.getCurrentInvitation());
					}
				}
			}
		}
	}

	@Override
	public void doInit() throws Exception {

		world = new World();
		world.setMyName(thisAgent.getAgentName());

		for (IAgentBean agentBean : thisAgent.getAgentBeans()) {
			if (agentBean instanceof StrategyBean) {
				playerBean = (StrategyBean) agentBean;
			}
		}
		playerBean.setWorld(world);
	}

	@Override
	public void doStart() throws Exception {

		super.doStart();

		sendAction = retrieveAction(ICommunicationBean.ACTION_SEND);
		memory.attach(new MessageObserver(), new JiacMessage());
	}

	public List<Log> getLogs() {
		return world.getLogs();
	}

	private Message getOldestInvitation() {

		Message oldestInvitation = null;

		for (Message invitation : world.getReceivedInvitations().values()) {
			if (oldestInvitation == null) {
				oldestInvitation = invitation;
			} else {
				if (invitation.getTimeStamp() < oldestInvitation.getTimeStamp()) {
					oldestInvitation = invitation;
				}
			}
		}

		return oldestInvitation;
	}

	private void receivedCancellingNegotiation(UUID invitationId, String agent) {

		if (world.isInviter() && world.getCurrentInvitation().getNegotiationID().equals(invitationId)) {

			sendInvitationRejected();

			world.setCurrentInvitation(null);
			world.setInviter(false);
			world.setProtocolPhase(null);
			begin();
		}
	}

	private void receivedConfirmingMyParticipation(UUID invitationId, String sender) {

		if (world.isInviter() && world.getCurrentInvitation().getNegotiationID().equals(invitationId)) {
			world.decreaseMissingReplies();

			if (world.getMissingReplies() == 0) {
				log.info(world.getMyName() + ": all agents confirmad their participation");

				Coalition newCoalition = world.getCoalition(((Set<String>) world.getCurrentInvitation().getContent()));
				newCoalition.setPayoffs(world.getNegotiatedPayoffs());

				Set<String> agentsLeavingOldCoalition = (Set<String>) ((HashSet<String>) world.getMyCoalition()
						.getAgents()).clone();
				agentsLeavingOldCoalition.retainAll(newCoalition.getAgents());
				Set<String> agentsLeavingInOldCoalition = (Set<String>) ((HashSet<String>) world.getMyCoalition()
						.getAgents()).clone();
				agentsLeavingInOldCoalition.removeAll(newCoalition.getAgents());
				if (!agentsLeavingInOldCoalition.isEmpty()) {
					sendLeavingCoalition(agentsLeavingOldCoalition, agentsLeavingInOldCoalition);
				}

				world.setMyCoalition(newCoalition);
				world.setMissingReplies(newCoalition.getSize() - 1);
				sendNewCoalition(newCoalition.getPayoffs());
			}
		}
	}

	private void receivedConfirmYourParticipation(Message message) {

		if (world.getUnsuccessfulNegotiations().contains(message.getNegotiationID())) {

			log.info(world.getMyName() + ": this negotiation was cancelled earlier, ignoring");
			world.getUnsuccessfulNegotiations().remove(message.getNegotiationID());

		} else {
			if (world.getCurrentInvitation().getNegotiationID().equals(message.getNegotiationID())) {
				world.setProtocolPhase(ProtocolPhase.KNOWLEDGE_EXCHANGE);
				sendConfirmingMyParticipation();

			} else {
				log.info(world.getMyName() + ": this is not my current neg, ignoring");
			}
		}
	}

	private void receivedDemand(Message demand) {

		if (world.isInviter() && world.getCurrentInvitation().getNegotiationID().equals(demand.getNegotiationID())) {

			world.getReceivedDemands().put(demand.getSender(), (Double) demand.getContent());
			world.decreaseMissingReplies();

			if (world.getMissingReplies() == 0) {
				analyseDemands();
			}
		}
	}

	private void receivedInvitation(Message invitation) {

		if (world.getUnsuccessfulNegotiations().contains(invitation.getNegotiationID())) {

			log.info(world.getMyName() + ": already received information that this negotiation was unsuccessful, ignoring");
			world.getUnsuccessfulNegotiations().remove(invitation.getNegotiationID());

		} else {
			if (world.isInNegotiationPhase()) {

				if (world.getCurrentInvitation().getTimeStamp() < invitation.getTimeStamp()) {

					log.info(world.getMyName() + ": received inv is younger, saving");
					world.getReceivedInvitations().put(invitation.getNegotiationID(), invitation);

				} else {

					if (world.isInInvitationPhase()) {

						log.info(world.getMyName() + ": canceling current negotiation " + " of "
								+ world.getCurrentInvitation().getSender());

						if (world.isInviter()) {
							sendNegotiationCancelled();
							world.setInviter(false);
						} else {
							sendCancellingNegotiation();
							world.getReceivedInvitations().remove(world.getCurrentInvitation().getNegotiationID());
						}

						double myDemand = playerBean.getMyDemand(world.getCoalition((Set<String>) invitation
								.getContent()));

						if (playerBean.isInvitationAcceptable(invitation)) {

							world.getReceivedInvitations().put(invitation.getNegotiationID(), invitation);
							world.setCurrentInvitation(invitation);
							sendDemand(myDemand);

						} else {
							sendRejectingInvitation(invitation);
							world.setCurrentInvitation(null);
							world.setProtocolPhase(null);
							begin();
						}

					} else {

						log.info(world.getMyName() + ": received inv ist older than current, but in inv phase, saving");
						world.getReceivedInvitations().put(invitation.getNegotiationID(), invitation);
					}
				}
			} else {
				world.getReceivedInvitations().put(invitation.getNegotiationID(), invitation);
				begin();
			}
		}
	}

	private void receivedInvitationRejected(UUID invitationId) {

		if (world.getReceivedInvitations().containsKey(invitationId)) {

			world.getReceivedInvitations().remove(invitationId);

			if (world.getCurrentInvitation().getNegotiationID().equals(invitationId)) {

				log.info(world.getMyName() + ": to byla currentInv");
				playerBean.negotiationUnsuccessful();
				world.setCurrentInvitation(null);
				world.setProtocolPhase(null);
				begin();
			}
		} else {
			world.getUnsuccessfulNegotiations().add(invitationId);
		}
	}

	private void receivedKnownAgents(Set<String> agents) {

		// leave only unknown agents
		agents.removeAll(world.getKnownAgents());
		agents.remove(world.getMyName());

		// update knowledge
		if (!agents.isEmpty()) {
			world.addAgents(agents);
			playerBean.addPreferredCoalitions(agents);
		}

		if (world.isInviter()) {

			world.decreaseMissingReplies();
			if (world.getMissingReplies() == 0) {
				// inform new coalition's members about known agents

				sendAgentsKnownToCoalition();

				world.getPreferredCoalitions().remove(
						world.getCoalition(((Set<String>) world.getCurrentInvitation().getContent())));
				world.setCurrentInvitation(null);
				world.setInviter(false);
				world.setProtocolPhase(null);

				begin();
			}
		} else {
			world.getReceivedInvitations().remove(world.getCurrentInvitation().getNegotiationID());
			world.setCurrentInvitation(null);
			world.setProtocolPhase(null);

			begin();
		}
	}

	private void receivedLeavingCoalition(Set<String> agents) {

		if (world.getMyCoalition().getAgents().containsAll(agents)) {
			// message received first time

			Set<String> myNewCoalitionAgents = (Set<String>) ((HashSet<String>) world.getMyCoalition().getAgents())
					.clone();
			myNewCoalitionAgents.removeAll(agents);
			Coalition myNewCoalition = world.getCoalition(myNewCoalitionAgents);
			Coalition myOldCoalition = world.getMyCoalition();
			if (myNewCoalition.getSize() > 1) {
				myNewCoalition.distributePayoffsProportionally(myOldCoalition);
			}
			world.setMyCoalition(myNewCoalition);

			log.info(world.getMyName() + ": my new coalition is " + myNewCoalition.getPayoffs());

			if (!world.isInNegotiationPhase()) {
				begin();
			}
		}
	}

	private void receivedNegotiationCancelled(UUID invitationId) {

		if (world.getReceivedInvitations().containsKey(invitationId)) {

			world.getReceivedInvitations().remove(invitationId);

			if (world.getCurrentInvitation().getNegotiationID().equals(invitationId)) {

				world.setCurrentInvitation(null);
				world.setProtocolPhase(null);
				begin();
			}
		} else {
			world.getUnsuccessfulNegotiations().add(invitationId);
		}
	}

	private void receivedNegotiationUnsuccessful(UUID invitationId) {
		receivedInvitationRejected(invitationId);
	}

	private void receivedNewCoalition(String sender, Map<String, Double> payoffs) {

		if (world.getMyCoalition().getSize() > 1) {
			Set<String> agentsLeavingOldCoalition = (Set<String>) ((HashSet<String>) world.getMyCoalition().getAgents())
					.clone();
			agentsLeavingOldCoalition.retainAll(payoffs.keySet());
			Set<String> agentsLeavingInOldCoalition = (Set<String>) ((HashSet<String>) world.getMyCoalition()
					.getAgents()).clone();
			agentsLeavingInOldCoalition.removeAll(payoffs.keySet());

			sendLeavingCoalition(agentsLeavingOldCoalition, agentsLeavingInOldCoalition);
		}

		// set my new coalition
		Coalition myNewCoalition = world.getCoalition(payoffs.keySet());
		myNewCoalition.setPayoffs((HashMap<String, Double>) payoffs);
		world.setMyCoalition(myNewCoalition);

		sendKnownAgents();
	}

	private void receivedRejectingInvitation(UUID invitationId) {

		if (world.isInviter() && world.getCurrentInvitation().getNegotiationID().equals(invitationId)) {

			sendInvitationRejected();

			world.getPreferredCoalitions().remove(
					world.getCoalition(((Set<String>) world.getCurrentInvitation().getContent())));
			world.setCurrentInvitation(null);
			world.setInviter(false);
			world.setProtocolPhase(null);
			begin();
		}
	}

	private void sendAgentsKnownToCoalition() {

		Message message = new Message(MessageInfo.AGENTS_KNOWN_TO_COALITION, world.getCurrentInvitation()
				.getNegotiationID(), world.getMyName(), world.getMyCoalition().getAgents(), world.getKnownAgents());
		sendMessage(message);
	}

	private void sendCancellingNegotiation() {

		Message message = new Message(MessageInfo.CANCELLING_NEGOTIATION, world.getCurrentInvitation()
				.getNegotiationID(), world.getMyName(), stringToSet(world.getCurrentInvitation().getSender()), null);
		sendMessage(message);
	}

	private void sendConfirmingMyParticipation() {

		Message message = new Message(MessageInfo.CONFIRMING_MY_PARTICIPATION, world.getCurrentInvitation()
				.getNegotiationID(), world.getMyName(), stringToSet(world.getCurrentInvitation().getSender()), null);
		sendMessage(message);
	}

	private void sendConfirmYourParticipation() {

		Message message = new Message(MessageInfo.CONFIRM_YOUR_PARTICIPATION, world.getCurrentInvitation()
				.getNegotiationID(), world.getMyName(), world.getCurrentInvitation().getReceivers(), null);
		sendMessage(message);
	}

	private void sendDemand(double demand) {

		Message message = new Message(MessageInfo.DEMAND, world.getCurrentInvitation().getNegotiationID(),
				world.getMyName(), stringToSet(world.getCurrentInvitation().getSender()), demand);
		sendMessage(message);
	}

	private void sendInvitation(Message invitation) {

		sendMessage(invitation);
	}

	private void sendInvitationRejected() {

		Message message = new Message(MessageInfo.INVITATION_REJECTED, world.getCurrentInvitation().getNegotiationID(),
				world.getMyName(), (Set<String>) world.getCurrentInvitation().getContent(), null);
		sendMessage(message);
	}

	private void sendKnownAgents() {

		Message message = new Message(MessageInfo.KNOWN_AGENTS, world.getCurrentInvitation().getNegotiationID(),
				world.getMyName(), stringToSet(world.getCurrentInvitation().getSender()), world.getKnownAgents());
		sendMessage(message);
	}

	private void sendLeavingCoalition(Set<String> agents, Set<String> receivers) {

		Message message = new Message(MessageInfo.LEAVING_COALITION, UUID.randomUUID(), world.getMyName(), receivers,
				agents);
		sendMessage(message);
	}

	private void sendMessage(Message message) {

		if (message.getReceivers().contains(world.getMyName())) {
			Set<String> receivers = new HashSet<String>(message.getReceivers());
			receivers.remove(world.getMyName());
			message.setReceivers(receivers);
		}
		
		world.logSentMessage(message);
		log.info(world.getMyName() + ": sending " + message.getInfo() + " {" + message.getContent() + "} to "
				+ message.getReceivers().toString());

		List<IAgentDescription> agents = thisAgent.searchAllAgents(new AgentDescription());
		for (IAgentDescription agent : agents) {
			if (message.getReceivers().contains(agent.getName())) {
				invoke(sendAction, new Serializable[] { new JiacMessage(message), agent.getMessageBoxAddress() });
			}
		}
	}

	private void sendNegotiationCancelled() {

		Message message = new Message(MessageInfo.NEGOTIATION_CANCELLED, world.getCurrentInvitation()
				.getNegotiationID(), world.getMyName(), (Set<String>) world.getCurrentInvitation().getContent(), null);
		sendMessage(message);
	}

	private void sendNegotiationUnsuccessful() {

		Message message = new Message(MessageInfo.NEGOTIATION_UNSUCCESSFUL, world.getCurrentInvitation()
				.getNegotiationID(), world.getMyName(), (Set<String>) world.getCurrentInvitation().getContent(), null);
		sendMessage(message);
	}

	private void sendNewCoalition(Map<String, Double> payoffs) {

		Message message = new Message(MessageInfo.NEW_COALITION, world.getCurrentInvitation().getNegotiationID(),
				world.getMyName(), payoffs.keySet(), payoffs);
		sendMessage(message);
	}

	private void sendRejectingInvitation(Message invitation) {

		Message message = new Message(MessageInfo.REJECTING_INVITATION, invitation.getNegotiationID(),
				world.getMyName(), stringToSet(invitation.getSender()), null);
		sendMessage(message);
	}

	public void setSimulatorModel(SimulatorModel simulatorModel) {
		this.simulatorModel = simulatorModel;
	}

	public void startSimulation() {
		begin();
	}

	private Set<String> stringToSet(String string) {

		Set<String> set = new HashSet<String>();
		set.add(string);
		return set;
	}
}
