package ontology;

import java.util.HashSet;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Agent implements IFact {

	private static final long serialVersionUID = 6214801768761251093L;

	private String name;
	private AgentStrategy behavior = null;
	private HashSet<String> knownAgents = null;
	private Coalition coaliton = null;
	private double payoff;

	public Agent() {
		super();
		knownAgents = new HashSet<String>();
	}

	public Agent(Agent agent) {
		super();
		this.name = agent.getName();
		this.knownAgents = new HashSet<String>(agent.getKnownAgents());
	}

	public Agent(String name) {
		super();
		this.name = name;
		knownAgents = new HashSet<String>();
	}

	public Agent(String name, AgentStrategy behavior, HashSet<String> knownAgents) {
		super();
		this.name = name;
		this.behavior = behavior;
		this.knownAgents = knownAgents;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Agent)) {
			return false;
		}
		return name.equals(((Agent) obj).getName());
	}

	public AgentStrategy getStrategy() {
		return behavior;
	}

	public Coalition getCoalition() {
		return coaliton;
	}

	public HashSet<String> getKnownAgents() {
		return knownAgents;
	}

	public String getName() {
		return name;
	}

	public double getPayoff() {
		return payoff;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	public void setCoalition(Coalition coaliton) {
		this.coaliton = coaliton;
	}

	public void setKnownAgents(HashSet<String> knownAgents) {
		this.knownAgents = knownAgents;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPayoff(double payoff) {
		this.payoff = payoff;
	}

	@Override
	public String toString() {
		return name;
	}
}
