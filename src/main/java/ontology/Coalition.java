package ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Coalition implements IFact, Comparable<Coalition> {

	private static final long serialVersionUID = 4873431560779779248L;

	private double value;
	private int size;
	private Set<String> agents = null;
	private HashMap<String, Double> payoffs = null;

	public Coalition(Coalition coalition) {
		super();
		this.value = coalition.getValue();
		this.size = coalition.getSize();
		this.agents = new HashSet<String>(coalition.getAgents());
		this.payoffs = new HashMap<String, Double>(coalition.getPayoffs());
	}

	public Coalition(double value, Set<String> agents) {
		super();
		this.value = value;
		this.size = agents.size();
		this.agents = agents;
		this.payoffs = new HashMap<String, Double>();

		// set payoffs for single coalition
		if (size == 1) {
			payoffs.put(agents.iterator().next(), value);
		}
	}

	/* compares values */
	@Override
	public int compareTo(Coalition coalition) {
		int result = -((Double) this.getValue()).compareTo(coalition.getValue());
		if (result == 0) {
			result = -1;
		}
		return result;
	}

	public boolean containsAgent(String agent) {
		return agents.contains(agent);
	}

	public void distributePayoffsProportionally(Coalition coalition) {

		double coalitionPayoffsSum = 0;
		for (String agent : agents) {
			coalitionPayoffsSum += coalition.getAgentPayoff(agent);
		}

		for (String agent : agents) {
			double agentPercentageShare = coalitionPayoffsSum / coalition.getAgentPayoff(agent);
			double agentPayoff = agentPercentageShare * value;
			payoffs.put(agent, agentPayoff);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coalition other = (Coalition) obj;
		if (agents == null) {
			if (other.agents != null)
				return false;
		} else if (!agents.equals(other.agents))
			return false;
		return true;
	}

	public double getAgentPayoff(String agent) {
		return payoffs.get(agent);
	}

	public Set<String> getAgents() {
		return agents;
	}

	public HashMap<String, Double> getPayoffs() {
		return payoffs;
	}

	public int getSize() {
		return size;
	}

	public double getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agents == null) ? 0 : agents.hashCode());
		return result;
	}

	public boolean isDisjunkt(Coalition c) {

		Iterator<String> i = agents.iterator();
		while (i.hasNext()) {
			if (c.agents.contains(i.next())) {
				return false;
			}
		}
		return true;
	}

	public void setAgents(HashSet<String> agents) {
		this.agents = agents;
	}

	public void setPayoffs(HashMap<String, Double> payoffs) {
		this.payoffs = payoffs;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Coalition [value=" + value + ", size=" + size + ", agents=" + agents + ", payoffs=" + payoffs + "]";
	}
}
