package game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ontology.Agent;
import ontology.AgentStrategy;
import ontology.Coalition;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GameParser {

	private Map<String, Agent> agents = null;
	private Map<Set<String>, Coalition> coalitions = null;
	private double coalitionDefaultValue;
	private String agentDafaultStrategy;

	public GameParser(File file) {

		Document dom = null;

		DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();
			dom = docBuild.parse(file);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		Element rootElement = dom.getDocumentElement();
		NodeList nodeList = rootElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				if ("configuration".equals(element.getNodeName())) {
					parseConfiguration(element);
				} else if ("agents".equals(element.getNodeName())) {
					parseAgents(element);
				} else if ("coalitions".equals(element.getNodeName())) {
					initCoalitions();
					parseCoalitions(element);
				}
			}
		}
	}

	public Map<String, Agent> getAgents() {
		return agents;
	}

	public double getCoalitionDefaultValue() {
		return coalitionDefaultValue;
	}

	public Map<Set<String>, Coalition> getCoalitions() {
		return coalitions;
	}

	private void initCoalitions() {
		coalitions = new HashMap<Set<String>, Coalition>();
		Set<Set<String>> agentsPowerSet = powerSet(agents.keySet());
		for (Set<String> agentsSet : agentsPowerSet) {
			if (agentsSet.size() > 0) {
				coalitions.put(agentsSet, new Coalition(coalitionDefaultValue, agentsSet));
			}
		}
	}

	private void parseAgents(Element element) {
		agents = new HashMap<String, Agent>();
		NodeList agentElements = element.getElementsByTagName("agent");
		for (int i = 0; i < agentElements.getLength(); i++) {
			Element agentElement = (Element) agentElements.item(i);
			String agentName = agentElement.getAttribute("name");
			String strategy = agentElement.getAttribute("strategy");
			AgentStrategy agentStrategy = null;
			if (strategy.isEmpty()) {
				strategy = agentDafaultStrategy;
			}
			if (strategy.equals("aggressive")) {
				agentStrategy = AgentStrategy.AGGRESSIVE;
			} else {
				agentStrategy = AgentStrategy.FAIR;
			}
			HashSet<String> knownAgents = parseKnownAgents((Element) agentElement.getElementsByTagName("knownAgents")
					.item(0));
			agents.put(agentName, new Agent(agentName, agentStrategy, knownAgents));
		}
	}

	private Set<String> parseCoalitionMembers(Element element) {
		Set<String> coalitionMembers = new HashSet<String>();
		NodeList memberElements = element.getElementsByTagName("member");
		for (int i = 0; i < memberElements.getLength(); i++) {
			Element memberElement = (Element) memberElements.item(i);
			coalitionMembers.add(memberElement.getAttribute("name"));
		}
		return coalitionMembers;
	}

	private void parseCoalitions(Element element) {
		NodeList coalitionElements = element.getElementsByTagName("coalition");
		for (int i = 0; i < coalitionElements.getLength(); i++) {
			Element coalitionElement = (Element) coalitionElements.item(i);
			double coalitionValue = Double.parseDouble(coalitionElement.getAttribute("value"));
			Set<String> coalitionMembers = parseCoalitionMembers((Element) coalitionElement.getElementsByTagName(
					"members").item(0));
			Coalition coalition = coalitions.get(coalitionMembers);
			coalition.setValue(coalitionValue);
		}
	}

	private void parseConfiguration(Element element) {
		coalitionDefaultValue = Double.parseDouble(element.getAttribute("coalitionDefaultValue"));
		agentDafaultStrategy = element.getAttribute("agentDafaultStrategy");
	}

	private HashSet<String> parseKnownAgents(Element element) {
		HashSet<String> knownAgents = new HashSet<String>();
		if (element != null) {
			NodeList agentElements = element.getElementsByTagName("knownAgent");
			for (int i = 0; i < agentElements.getLength(); i++) {
				Element agentElement = (Element) agentElements.item(i);
				knownAgents.add(agentElement.getAttribute("name"));
			}
		}
		return knownAgents;
	}

	private Set<Set<String>> powerSet(Set<String> originalSet) {
		Set<Set<String>> sets = new HashSet<Set<String>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<String>());
			return sets;
		}
		List<String> list = new ArrayList<String>(originalSet);
		String head = list.get(0);
		Set<String> rest = new HashSet<String>(list.subList(1, list.size()));
		for (Set<String> set : powerSet(rest)) {
			Set<String> newSet = new HashSet<String>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}
}
