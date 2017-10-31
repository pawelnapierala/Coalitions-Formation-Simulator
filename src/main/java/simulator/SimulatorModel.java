package simulator;

import game.Game;
import game.GameParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ontology.Agent;
import ontology.AgentStrategy;
import ontology.Coalition;
import ontology.Log;
import ontology.LogInfo;
import ontology.Message;
import ontology.MessageEdge;
import ontology.Step;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import beans.ProtocolBean;
import beans.StrategyBean;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class SimulatorModel implements IModel {

	private List<IObserver> observers = new LinkedList<IObserver>();

	private SimpleAgentNode simulationNode = null;
	private List<Log> logs = null;

	private Game game = null;

	private Map<String, Agent> currentAgents = null;
	private Map<Set<String>, Coalition> currentCoalitions = null;
	private List<Step> steps = null;
	private Step currentStep;

	private List<Coalition> finalCoalitions = null;
	private Map<String, Agent> finalAgents = null;
	private boolean finalCoalitionsInCore;

	private Graph<String, Object> coalitionsFormationsGraph = null;
	private Graph<String, String> knownAgentsGraph = null;

	private boolean simulationFinished = false;
	private String pickedCoalitionsFormationsGraphVertex = null;
	private String pickedKnownAgentsGraphVertex = null;

	private Set<String> readyAgents = null;

	private void addDirectedEdge(Graph<String, String> graph, String vertex1, String vertex2) {
		String edge = "Edge-" + vertex1 + "-" + vertex2;
		graph.addEdge(edge, vertex1, vertex2, EdgeType.DIRECTED);
	}

	private Map<String, Agent> collectAgents() {
		Map<String, Agent> agents = new HashMap<String, Agent>();

		for (StrategyBean playerBean : getPlayerBeans()) {
			Agent agent = playerBean.getSelf();
			agents.put(agent.getName(), agent);
		}

		return agents;
	}

	private Set<Coalition> collectCoalitions() {
		Set<Coalition> coalitions = new HashSet<Coalition>();

		for (StrategyBean playerBean : getPlayerBeans()) {
			Coalition agentCoalition = playerBean.getSelf().getCoalition();
			coalitions.add(agentCoalition);
		}

		return coalitions;
	}

	private List<Log> collectLogs() {
		List<Log> logs = new LinkedList<Log>();

		for (ProtocolBean protocolBean : getProtocolBeans()) {
			List<Log> agentLogs = protocolBean.getLogs();
			logs.addAll(agentLogs);
		}

		return logs;
	}

	@Override
	public void computeCoalitionsFormations() {
		initCurrentCoalitions();
		initCurrentAgents();
		initCoalitionsFormationsGraph();

		logs = collectLogs();
		Collections.sort(logs);

		currentStep = new Step(0, LogInfo.INITIAL_STEP, null, null, null, new HashSet<Coalition>(
				currentCoalitions.values()), null, null, null);
		steps = new LinkedList<Step>();
		steps.add(currentStep);

		for (IObserver observer : observers) {
			observer.updateStepNr();
			observer.updateStepInfo();
			observer.updateCoalitionsFormationsGraph();
		}
	}

	@Override
	public void computeFinalAgents() {
		finalAgents = collectAgents();

		for (IObserver observer : observers) {
			observer.updateFinalAgents();
		}
	}

	@Override
	public void computeFinalCoalitions() {
		finalCoalitions = new ArrayList<Coalition>(collectCoalitions());
		Collections.sort(finalCoalitions);

		for (IObserver observer : observers) {
			observer.updateFinalCoalitions();
		}
	}

	@Override
	public void computeGameProperties() {

		game.computeProperties();

		if (simulationFinished) {
			finalCoalitionsInCore = game.inCore(getPayoffsUnion(finalCoalitions));
		}

		for (IObserver observer : observers) {
			observer.updateGameProperties();
		}
	}

	@Override
	public void computeKnownAgentsGraph() {
		knownAgentsGraph = new SparseMultigraph<String, String>();

		for (Agent agent : finalAgents.values()) {
			knownAgentsGraph.addVertex(agent.getName());

			for (String knownAgent : agent.getKnownAgents()) {
				addDirectedEdge(knownAgentsGraph, agent.getName(), knownAgent);
			}
		}

		for (IObserver observer : observers) {
			observer.updateKnownAgentsGraph();
		}
	}

	private void createConfFile() {
		String nl = System.getProperty("line.separator");

		Path path = Paths.get("configuration.xml");
		try {
			BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + nl);
			bw.write("<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" "
					+ "\"http://www.springframework.org/dtd/spring-beans.dtd\">" + nl);
			bw.write("<beans>" + nl);
			bw.write("<import resource=\"classpath:de/dailab/jiactng/agentcore/conf/AgentNode.xml\" />" + nl);
			bw.write("<import resource=\"classpath:de/dailab/jiactng/agentcore/conf/Agent.xml\" />" + nl);
			bw.write("<import resource=\"classpath:de/dailab/jiactng/agentcore/conf/JMSMessaging.xml\" />" + nl);
			bw.write("<bean name=\"CoalitionFormationNode\" parent=\"NodeWithJMX\">" + nl);
			bw.write("<property name=\"agents\">" + nl);
			bw.write("<list>" + nl);

			for (String agent : game.getAgents().keySet()) {
				bw.write("<ref bean=\"" + agent + "\" />" + nl);
			}

			bw.write("</list>" + nl);
			bw.write("</property>" + nl);
			bw.write("</bean>" + nl);

			for (String agent : game.getAgents().keySet()) {
				String strategy = game.getAgents().get(agent).getStrategy().equals(AgentStrategy.AGGRESSIVE) ? "Aggressive"
						: "Fair";
				bw.write("<bean name=\"" + agent + "\" parent=\"SimpleAgent\" singleton=\"false\">" + nl);
				bw.write("<property name=\"agentBeans\">" + nl);
				bw.write("<list>" + nl);
				bw.write("<ref bean=\"ProtocolBean_" + agent + "\" />" + nl);
				bw.write("<ref bean=\"" + strategy + "StrategyBean_" + agent + "\" />" + nl);
				bw.write("</list>" + nl);
				bw.write("</property>" + nl);
				bw.write("</bean>" + nl);
			}

			for (Agent agent : game.getAgents().values()) {
				String strategy = agent.getStrategy().equals(AgentStrategy.AGGRESSIVE) ? "Aggressive" : "Fair";
				bw.write("<bean name=\"" + strategy + "StrategyBean_" + agent.getName() + "\" parent=\"" + strategy
						+ "StrategyBean\" singleton=\"false\">" + nl);
				bw.write("<property name=\"knownAgentsParam\" value=\"");

				Iterator<String> i1 = agent.getKnownAgents().iterator();
				while (i1.hasNext()) {
					String knownAgent = i1.next();
					bw.write(knownAgent + (i1.hasNext() ? "," : ""));
				}

				bw.write("\" />" + nl);
				bw.write("<property name=\"coalitionsValuesParam\" value=\"");

				Iterator<Coalition> i2 = game.getCoalitions().values().iterator();
				while (i2.hasNext()) {
					Coalition coalition = i2.next();
					if (coalition.getAgents().contains(agent.getName())) {
						Iterator<String> i3 = coalition.getAgents().iterator();
						while (i3.hasNext()) {
							String coalitionMember = i3.next();
							bw.write(coalitionMember + (i3.hasNext() ? "," : ""));
						}
						bw.write("=" + coalition.getValue() + ";");
					}
				}

				bw.write("\" />" + nl);
				bw.write("</bean>" + nl);

				bw.write("<bean name=\"ProtocolBean_" + agent.getName()
						+ "\" parent=\"ProtocolBean\" singleton=\"false\">" + nl);
				bw.write("</bean>" + nl);
			}

			bw.write("<bean name=\"FairStrategyBean\" class=\"beans.FairStrategyBean\" abstract=\"true\">" + nl);
			bw.write("<property name=\"logLevel\" value=\"INFO\" />" + nl);
			bw.write("</bean>" + nl);

			bw.write("<bean name=\"AggressiveStrategyBean\" class=\"beans.AggressiveStrategyBean\" abstract=\"true\">"
					+ nl);
			bw.write("<property name=\"logLevel\" value=\"INFO\" />" + nl);
			bw.write("</bean>" + nl);

			bw.write("<bean name=\"ProtocolBean\" class=\"beans.ProtocolBean\" abstract=\"true\">" + nl);
			bw.write("<property name=\"logLevel\" value=\"INFO\" />" + nl);
			bw.write("</bean>" + nl);

			bw.write("</beans>" + nl);

			bw.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Graph<String, Object> getCoalitionsFormationsGraph() {
		return coalitionsFormationsGraph;
	}

	@Override
	public Agent getCoalitionsFormationsGraphAgent(String agent) {
		return currentAgents.get(agent);
	}

	@Override
	public String getCoalitionsFormationsGraphPickedVertex() {
		return pickedCoalitionsFormationsGraphVertex;
	}

	public Graph<String, Object> getCurrentCoalitionsGraph() {
		return coalitionsFormationsGraph;
	}

	@Override
	public Set<String> getCurrentlyKnownAgents(String agent) {
		return currentAgents.get(agent).getKnownAgents();
	}

	@Override
	public Step getCurrentStep() {
		return currentStep;
	}

	@Override
	public LogInfo getCurrentStepInfo() {
		return currentStep.getInfo();
	}

	@Override
	public int getCurrentStepNr() {
		return currentStep.getStepNr();
	}

	@Override
	public List<Coalition> getFinalCoalitions() {
		return finalCoalitions;
	}

	@Override
	public Set<String> getFinalKnownAgents(String agent) {
		return finalAgents.get(agent).getKnownAgents();
	}

	@Override
	public Game getGame() {
		return game;
	}

	@Override
	public Graph getKnownAgentsGraph() {
		return knownAgentsGraph;
	}

	@Override
	public Agent getKnownAgentsGraphAgent(String agent) {
		return finalAgents.get(agent);
	}

	@Override
	public String getKnownAgentsGraphPickedVertex() {
		return pickedKnownAgentsGraphVertex;
	}

	private Set<MessageEdge> getMessagesToAdd(Log log) {
		Message message = (Message) log.getContent();
		Set<MessageEdge> edges = new HashSet<MessageEdge>();

		if (message.getSender() == log.getAuthor()) { 	// add only sent messages, no received msgs
			for (String receiver : message.getReceivers()) {
				MessageEdge edge = new MessageEdge(message, message.getSender(), receiver);
				edges.add(edge);
			}
		}

		return edges;
	}

	private Set<MessageEdge> getMessagesToRemove(Set<MessageEdge> addedEdges) {
		Set<MessageEdge> edgesToRemove = new HashSet<MessageEdge>();

		// remove all opposite messages to added messages
		for (MessageEdge addedEdge : addedEdges) {
			// find opposite messages
			Collection<Object> oppositeEdges = coalitionsFormationsGraph.findEdgeSet(addedEdge.getTo(),
					addedEdge.getFrom());
			for (Object oppositeEdge : oppositeEdges) {
				if (oppositeEdge instanceof MessageEdge) {
					MessageEdge oppositeMessageEdge = (MessageEdge) oppositeEdge;
					if (oppositeMessageEdge.getNegotiationID().equals(addedEdge.getNegotiationID())) {
						edgesToRemove.add(oppositeMessageEdge);
					}
				}
			}
		}

		if ((getCurrentStep().getInfo().equals(LogInfo.RECEIVED_INVITATION_REJECTED)
				|| getCurrentStep().getInfo().equals(LogInfo.RECEIVED_NEGOTIATION_CANCELLED)
				|| getCurrentStep().getInfo().equals(LogInfo.RECEIVED_NEGOTIATION_UNSUCCESSFUL) || getCurrentStep()
				.getInfo().equals(LogInfo.RECEIVED_AGENTS_KNOWN_TO_COALITION))
				&& currentStep.getReceivedMessage() != null) {

			Collection<Object> incidentEdges = coalitionsFormationsGraph.getIncidentEdges(getCurrentStep()
					.getReceivedMessage().getTo());
			for (Object incidentEdge : incidentEdges) {
				if (incidentEdge instanceof MessageEdge) {
					MessageEdge incidentMsgEdge = (MessageEdge) incidentEdge;
					if (incidentMsgEdge.getNegotiationID().equals(
							getCurrentStep().getReceivedMessage().getNegotiationID())) {
						edgesToRemove.add(incidentMsgEdge);
					}
				}
			}
		}

		return edgesToRemove;
	}

	private Set<String> getNewKnownAgents(Log log) {
		Set<String> newKnownAgents = null;

		if (LogInfo.RECEIVED_KNOWN_AGENTS.equals(log.getInfo())
				|| LogInfo.RECEIVED_AGENTS_KNOWN_TO_COALITION.equals(log.getInfo())) {
			newKnownAgents = (Set<String>) ((HashSet<String>) ((Message) log.getContent()).getContent()).clone();
			newKnownAgents.removeAll(currentAgents.get(log.getAuthor()).getKnownAgents());
		}

		if (newKnownAgents != null && !newKnownAgents.isEmpty()) {
			return newKnownAgents;
		} else {
			return null;
		}
	}

	@Override
	public int getNumberOfSteps() {
		return logs.size() + 1;
	}

	private Map<String, Double> getPayoffsUnion(Collection<Coalition> coalitions) {
		Map<String, Double> payoffs = new HashMap<String, Double>();
		for (Coalition coalition : coalitions) {
			payoffs.putAll(coalition.getPayoffs());
		}
		return payoffs;
	}

	private List<StrategyBean> getPlayerBeans() {
		List<StrategyBean> playerBeans = new LinkedList<StrategyBean>();

		List<IAgent> agents = simulationNode.findAgents();
		for (IAgent agent : agents) {
			if (!agent.getAgentName().equals("Simulator")) {
				StrategyBean playerBean = agent.findAgentBean(StrategyBean.class);
				playerBeans.add(playerBean);
			}
		}

		return playerBeans;
	}

	private List<ProtocolBean> getProtocolBeans() {
		List<ProtocolBean> protocolBeans = new LinkedList<ProtocolBean>();

		List<IAgent> agents = simulationNode.findAgents();
		for (IAgent agent : agents) {
			if (!agent.getAgentName().equals("Simulator")) {
				ProtocolBean protocolBean = agent.findAgentBean(ProtocolBean.class);
				protocolBeans.add(protocolBean);
			}
		}

		return protocolBeans;
	}

	private MessageEdge getReceivedMessage(Log log) {
		Message message = (Message) log.getContent();

		if (log.receivedMessage()) {
			// received message
			Collection<Object> edgesToReceiver = coalitionsFormationsGraph.findEdgeSet(message.getSender(),
					log.getAuthor());
			for (Object edge : edgesToReceiver) {
				// find sent message
				if (edge instanceof MessageEdge && ((MessageEdge) edge).equals(message)) {
					return (MessageEdge) edge;
				}
			}
		}

		return null;
	}

	@Override
	public void goToStep(int goToStepNr) {
		int currentStepNr = currentStep.getStepNr();

		if (goToStepNr > currentStepNr) {
			for (int i = currentStepNr; i < goToStepNr; i++) {
				stepForward();
			}
		} else {
			for (int i = currentStepNr; i > goToStepNr; i--) {
				stepBackward();
			}
		}

		for (IObserver observer : observers) {
			observer.updateStepNr();
			observer.updateStepInfo();
			observer.updateCoalitionsFormationsGraph();
		}
	}

	private void initCoalitionsFormationsGraph() {
		coalitionsFormationsGraph = new SparseMultigraph<String, Object>();

		for (String agent : game.getAgents().keySet()) {
			coalitionsFormationsGraph.addVertex(agent);
		}
	}

	private void initCurrentAgents() {
		currentAgents = new HashMap<String, Agent>();

		for (Agent confAgent : game.getAgents().values()) {

			Coalition currentAgentCoalition = currentCoalitions.get(stringToSet(confAgent.getName()));

			Agent currentAgent = new Agent(confAgent);
			currentAgent.setCoalition(currentAgentCoalition);
			currentAgent.setPayoff(currentAgentCoalition.getValue());

			currentAgents.put(currentAgent.getName(), currentAgent);
		}
	}

	private void initCurrentCoalitions() {
		currentCoalitions = new HashMap<Set<String>, Coalition>();

		for (Coalition coalition : game.getCoalitions().values()) {
			// there are only single coalitions at the beginning
			if (coalition.getSize() == 1) {
				Coalition singleCoalition = new Coalition(coalition);
				currentCoalitions.put(coalition.getAgents(), singleCoalition);
			}
		}
	}

	@Override
	public boolean isSimulationFinished() {
		return simulationFinished;
	}

	@Override
	public boolean isSimulationResultInCore() {
		return finalCoalitionsInCore;
	}

	@Override
	public void loadGameFromFile(File file) {

		GameParser parser = new GameParser(file);
		game = new Game(parser.getAgents(), parser.getCoalitions());
	}

	@Override
	public void pickCoalitionsFormationsGraphVertex(String vertex) {
		pickedCoalitionsFormationsGraphVertex = vertex;

		for (IObserver observer : observers) {
			observer.updateCoalitionsFormationsGraphPickedVertex();
		}
	}

	@Override
	public void pickKnownAgentsGraphVertex(String vertex) {
		pickedKnownAgentsGraphVertex = vertex;

		for (IObserver observer : observers) {
			observer.updateKnownAgentsGraphPickedVertex();
		}
	}

	@Override
	public void registerObserver(IObserver o) {
		observers.add(o);
	}

	public void removeObserver(IObserver o) {
		int oIndex = observers.indexOf(o);
		if (oIndex >= 0) {
			observers.remove(oIndex);
		}
	}

	public void setAgentReady(String agent, boolean ready) {
		if (ready) {
			readyAgents.add(agent);
			if (readyAgents.size() == game.getAgents().size()) {
				simulationFinished();
			}
		} else {
			readyAgents.remove(agent);
		}
	}

	public void simulationFinished() {
		simulationFinished = true;

		// give agents a little time to finish
		wait(100);

		for (IObserver observer : observers) {
			observer.updateSimulationFinished();
		}
	}

	@Override
	public void startSimulation() {
		logs = new LinkedList<Log>();
		readyAgents = new HashSet<String>();

		createConfFile();

		simulationNode = (SimpleAgentNode) new FileSystemXmlApplicationContext("configuration.xml")
				.getBean("CoalitionFormationNode");

		for (ProtocolBean protocolBean : getProtocolBeans()) {
			protocolBean.setSimulatorModel(this);
			protocolBean.startSimulation();
		}
	}

	private void stepBackward() {
		updateAgents(currentAgents, currentStep.getRemovedCoalitions(), currentStep.getNewKnownAgents(),
				currentStep.getAgent(), false);
		updateCoalitionsGraphCoalitions(currentStep.getRemovedCoalitions(), currentStep.getAddedCoalitions());
		updateCoalitionsGraphMessages(currentStep.getRemovedMessages(), currentStep.getAddedMessages(),
				currentStep.getReceivedMessage(), false);
		Step prevStep = steps.get(currentStep.getStepNr() - 1);
		currentStep = prevStep;
	}

	private void stepForward() {
		Step nextStep = null;

		if (steps.size() > (getCurrentStepNr() + 1)) {
			nextStep = steps.get(getCurrentStepNr() + 1);

		} else {
			// compute next step

			Log log = logs.get(getCurrentStepNr());
			Message message = (Message) log.getContent();

			Set<MessageEdge> addedMessages = getMessagesToAdd(log);
			Set<MessageEdge> removedMessages = getMessagesToRemove(addedMessages);
			MessageEdge receivedMessage = getReceivedMessage(log);
			Set<String> newKnownAgents = getNewKnownAgents(log);
			Set<Coalition> newCoalitions = null;
			Set<Coalition> oldCoalitions = null;

			if (LogInfo.SENT_NEW_COALITION.equals(log.getInfo()) || LogInfo.SENT_LEAVING_COALITION.equals(log.getInfo())) {
				// changes in coalitions

				Coalition newCoalition = null;

				if (LogInfo.SENT_NEW_COALITION.equals(log.getInfo())) {
					HashMap<String, Double> payoffs = (HashMap<String, Double>) message.getContent();
					newCoalition = new Coalition(game.getCoalitions().get(payoffs.keySet()));
					newCoalition.setPayoffs(payoffs);

				} else {
					if (currentAgents.get(log.getAuthor()).getCoalition().getAgents()
							.containsAll(message.getReceivers())) {
						newCoalition = new Coalition(game.getCoalitions().get(stringToSet(log.getAuthor())));
					}
				}

				if (newCoalition != null) {
					newCoalitions = new HashSet<Coalition>();
					oldCoalitions = new HashSet<Coalition>();
					newCoalitions.add(newCoalition);

					// get all old coalitions
					for (String agentName : newCoalition.getAgents()) {
						Agent agent = currentAgents.get(agentName);
						if (!oldCoalitions.contains(agent.getCoalition())) {
							oldCoalitions.add(agent.getCoalition());
						}
					}

					// get all new coalitions
					for (Coalition oldCoalition : oldCoalitions) {
						currentCoalitions.remove(oldCoalition.getAgents());

						Set<String> leftAgents = new HashSet<String>(oldCoalition.getAgents());
						leftAgents.removeAll(newCoalition.getAgents());

						if (!leftAgents.isEmpty()) {
							Coalition leftCoalition = new Coalition(game.getCoalitions().get(leftAgents));
							leftCoalition.distributePayoffsProportionally(oldCoalition);

							newCoalitions.add(leftCoalition);
						}
					}
				}
			}
			nextStep = new Step(getCurrentStepNr() + 1, log.getInfo(), addedMessages, removedMessages, receivedMessage,
					newCoalitions, oldCoalitions, newKnownAgents, log.getAuthor());
			steps.add(nextStep);
		}

		updateAgents(currentAgents, nextStep.getAddedCoalitions(), nextStep.getNewKnownAgents(), nextStep.getAgent(),
				true);
		updateCoalitionsGraphCoalitions(nextStep.getAddedCoalitions(), nextStep.getRemovedCoalitions());
		updateCoalitionsGraphMessages(nextStep.getAddedMessages(), nextStep.getRemovedMessages(),
				nextStep.getReceivedMessage(), true);
		currentStep = nextStep;
	}

	private Set<String> stringToSet(String string) {
		Set<String> singleSet = new HashSet<String>();
		singleSet.add(string);
		return singleSet;
	}

	@Override
	public void unpickCoalitionsFormationsGraphVertex() {
		pickedCoalitionsFormationsGraphVertex = null;

		for (IObserver observer : observers) {
			observer.updateCoalitionsFormationsGraphPickedVertex();
		}
	}

	@Override
	public void unpickKnownAgentsGraphVertex() {
		pickedKnownAgentsGraphVertex = null;

		for (IObserver observer : observers) {
			observer.updateKnownAgentsGraphPickedVertex();
		}
	}

	private void updateAgents(Map<String, Agent> agents, Collection<Coalition> coalitions, Set<String> newKnownAgents,
			String agent, boolean goingForward) {

		if (coalitions != null) {
			for (Coalition coalition : coalitions) {
				for (String agentName : coalition.getAgents()) {
					Agent a = agents.get(agentName);
					a.setCoalition(coalition);
					a.setPayoff(coalition.getAgentPayoff(agentName));
				}
			}
		}

		if (newKnownAgents != null) {
			if (goingForward) {
				currentAgents.get(agent).getKnownAgents().addAll(newKnownAgents);
			} else {
				currentAgents.get(agent).getKnownAgents().removeAll(newKnownAgents);
			}
		}
	}

	private void updateCoalitionsGraphCoalitions(Collection<Coalition> newCoalitions,
			Collection<Coalition> oldCoalitions) {
		// remove all edges from old coalitions
		if (oldCoalitions != null) {
			for (Coalition oldCoalition : oldCoalitions) {
				for (String agent : oldCoalition.getAgents()) {
					for (Object edge : coalitionsFormationsGraph.getIncidentEdges(agent)) {
						if (edge instanceof String) { // nie usuwaj messages
							coalitionsFormationsGraph.removeEdge(edge);
						}
					}
				}
			}
		}

		// connect all members of new coalitions
		if (newCoalitions != null) {
			for (Coalition newCoalition : newCoalitions) {
				for (String agent1 : newCoalition.getAgents()) {
					for (String agent2 : newCoalition.getAgents()) {
						if (!agent1.equals(agent2)
								&& !coalitionsFormationsGraph.containsEdge("Edge-" + agent1 + "-" + agent2)
								&& !coalitionsFormationsGraph.containsEdge("Edge-" + agent2 + "-" + agent1)) {
							coalitionsFormationsGraph.addEdge("Edge-" + agent1 + "-" + agent2, agent1, agent2,
									EdgeType.UNDIRECTED);
						}
					}
				}
			}
		}
	}

	private void updateCoalitionsGraphMessages(Collection<MessageEdge> messagesToAdd,
			Collection<MessageEdge> messagesToRemove, MessageEdge receivedMessage, boolean goingForward) {
		// message received
		if (receivedMessage != null) {
			receivedMessage.setReceived(goingForward);
		}

		// remove messages
		if (messagesToRemove != null) {
			for (MessageEdge message : messagesToRemove) {
				coalitionsFormationsGraph.removeEdge(message);
			}
		}

		// add messages
		if (messagesToAdd != null) {
			for (MessageEdge message : messagesToAdd) {
				coalitionsFormationsGraph.addEdge(message, message.getFrom(), message.getTo(), EdgeType.DIRECTED);
			}
		}
	}

	private void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
