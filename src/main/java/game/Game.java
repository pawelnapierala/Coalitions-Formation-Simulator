package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ontology.Agent;
import ontology.Coalition;

/**
 * 
 */

/**
 * @author loomin
 * 
 */
public class Game {

	private final double PRECISION_CONSTANT = 0.001;

	private boolean superadditiv;
	private boolean subadditiv;
	private boolean convex;

	String superadditivityProof = null;
	String subadditivityProof = null;
	String convexityProof = null;

	private Map<String, Agent> agents = null;
	private Map<Set<String>, Coalition> coalitions = null;

	private ArrayList<String> players = null;

	public Game(Map<String, Agent> agents, Map<Set<String>, Coalition> coalitions) {
		this.agents = agents;
		this.coalitions = coalitions;

		players = new ArrayList<String>(agents.keySet());
	}

	private double addUpValues(Collection<Double> collection) {

		double sum = 0;
		for (Double value : collection) {
			sum += value;
		}

		return sum;
	}

	public Collection<Coalition> computeBestCoalitionStructure() {

		Set<Set<Coalition>> coalitionStructures = generatePossibleCoalitionStructures();

		/* find coalition structure with highest value */
		Set<Coalition> bestCoalitionStructure = null;
		double bestValue = -1;
		for (Set<Coalition> coalitionStructure : coalitionStructures) {
			double coalitionStructureValue = 0;
			for (Coalition coalition : coalitionStructure) {
				coalitionStructureValue += coalition.getValue();
			}
			if (coalitionStructureValue > bestValue) {
				bestCoalitionStructure = coalitionStructure;
				bestValue = coalitionStructureValue;
			}
		}

		return bestCoalitionStructure;
	}

	public Map<String, Double> computeKernel(Collection<Coalition> coalitionStructure) {

		double structureValue = 0;
		Map<String, Double> payoffs = new HashMap<String, Double>();
		for (Coalition coalition : coalitionStructure) {
			structureValue += coalition.getValue();
			payoffs.putAll(coalition.getPayoffs());
		}

		double g;
		String a1 = null;
		String a2 = null;

		do {
			g = Double.NEGATIVE_INFINITY;
			for (Coalition coalition : coalitionStructure) {
				List<String> agents = new ArrayList<String>(coalition.getAgents());
				for (int i = 0; i < agents.size(); i++) {
					String agent1 = agents.get(i);
					for (int j = i + 1; j < agents.size(); j++) {
						String agent2 = agents.get(j);
						double maxSurplusA1A2 = maxSurplus(payoffs, agent1, agent2);
						double maxSurplusA2A1 = maxSurplus(payoffs, agent2, agent1);
						double gA1A2 = maxSurplusA1A2 - maxSurplusA2A1;
						double gA2A1 = maxSurplusA2A1 - maxSurplusA1A2;
						if (gA1A2 > g) {
							g = gA1A2;
							a1 = agent1;
							a2 = agent2;
						} else if (gA2A1 > g) {
							g = gA2A1;
							a1 = agent2;
							a2 = agent1;
						}
					}
				}
			}

			if (g > Double.NEGATIVE_INFINITY) {
				double d = 0;
				if (Math.abs(payoffs.get(a2) - getCoalition(a2).getValue()) < g / 2
						&& Math.abs(payoffs.get(a2) - getCoalition(a2).getValue()) != 0) {
					d = Math.abs(payoffs.get(a2) - getCoalition(a2).getValue());
				} else {
					d = g / 2;
				}

				payoffs.put(a1, payoffs.get(a1) + d);
				payoffs.put(a2, payoffs.get(a2) - d);
			}
		} while ((g / structureValue) > PRECISION_CONSTANT);

		return payoffs;
	}

	public void computeProperties() {

		Coalition[] coalitionsArray = this.coalitions.values().toArray(new Coalition[0]);

		superadditiv = true;
		subadditiv = true;
		convex = true;

		for (int i = 0; i < coalitionsArray.length; i++) {
			for (int j = i; j < coalitionsArray.length; j++) {
				Coalition ci = coalitionsArray[i];
				Coalition cj = coalitionsArray[j];

				Coalition union = getUnion(ci, cj);
				Coalition intersection = getIntersection(ci, cj);

				double intersectionValue = 0;
				String intersectionName = "\u2205";
				if (intersection != null) {
					intersectionValue = intersection.getValue();
					intersectionName = intersection.getAgents().toString();
				}

				// checks if the game is superadditive / subadditive
				if (ci.isDisjunkt(cj)) {
					if (union.getValue() < ci.getValue() + cj.getValue()) {
						superadditiv = false;
						superadditivityProof = union.getAgents() + "(" + union.getValue() + ") \u2271 "
								+ ci.getAgents() + "(" + ci.getValue() + ") + " + cj.getAgents() + "(" + cj.getValue()
								+ ")\n";
					} else {
						subadditiv = false;
						subadditivityProof = union.getAgents() + "(" + union.getValue() + ") \u226E " + ci.getAgents()
								+ "(" + ci.getValue() + ") + " + cj.getAgents() + "(" + cj.getValue() + ")\n";
					}
				}

				// checks if the game is convex
				if (union.getValue() < ci.getValue() + cj.getValue() - intersectionValue) {
					convex = false;
					convexityProof = union.getValue() + "(" + union.getAgents() + ") \u2271 " + ci.getValue() + "("
							+ ci.getAgents() + ") + " + cj.getValue() + "(" + cj.getAgents() + ") + "
							+ intersectionValue + "(" + intersectionName + ")\n";
				}
			}
		}
	}

	public Map<String, Double> computeShapleyValue() {

		Map<String, Double> values = new HashMap<String, Double>();
		int n = agents.size();
		double factN = factorial(n);

		for (String player : agents.keySet()) {
			double value = 0;
			for (Coalition coalition : coalitions.values()) {
				if (!coalition.getAgents().contains(player)) {
					int s = coalition.getSize();
					int factS = factorial(s);
					Coalition union = getUnion(coalition, getCoalition(player));
					value += (factS * factorial(n - s - 1) * (union.getValue() - coalition.getValue())) / factN;
				}
			}
			values.put(player, value);
		}
		return values;
	}

	private void createCoalitionStructure(Set<Set<Coalition>> coalitionStructures, Set<Coalition> coalitionStructure,
			int sizeLeft) {

		if (sizeLeft == 0) {
			if (!coalitionStructures.contains(coalitionStructure)) {
				coalitionStructures.add(coalitionStructure);
			}
		} else {
			for (Coalition coalition : coalitions.values()) {
				if (coalition.getSize() <= sizeLeft && !coalitionStructure.contains(coalition)
						&& disjunkt(coalitionStructure, coalition)) {
					Set<Coalition> coalitionStructureDuplicate = (Set<Coalition>) ((HashSet<Coalition>) coalitionStructure)
							.clone();
					coalitionStructureDuplicate.add(coalition);
					createCoalitionStructure(coalitionStructures, coalitionStructureDuplicate,
							sizeLeft - coalition.getSize());
				}
			}
		}
	}

	private boolean disjunkt(Set<Coalition> coalitionStructure, Coalition coalition) {
		for (String agent : coalition.getAgents()) {
			for (Coalition c : coalitionStructure) {
				if (c.containsAgent(agent)) {
					return false;
				}
			}
		}
		return true;
	}

	private double excess(Coalition coalition, Map<String, Double> payoffDistribution) {

		double sum = 0;
		for (String agent : coalition.getAgents()) {
			sum += payoffDistribution.get(agent);
		}

		return coalition.getValue() - sum;
	}

	private int factorial(int n) {

		int factorial = 1;
		for (int i = 1; i <= n; i++) {
			factorial *= i;
		}
		return factorial;
	}

	private Set<Set<Coalition>> generatePossibleCoalitionStructures() {

		Set<Set<Coalition>> coalitionStructures = new HashSet<Set<Coalition>>();

		createCoalitionStructure(coalitionStructures, new HashSet<Coalition>(), players.size());

		return coalitionStructures;
	}

	public Map<String, Agent> getAgents() {
		return agents;
	}

	private Coalition getCoalition(String player) {
		HashSet<String> set = new HashSet<String>();
		set.add(player);
		return coalitions.get(set);
	}

	public Map<Set<String>, Coalition> getCoalitions() {
		return coalitions;
	}

	public String getConvexityProof() {
		return convexityProof;
	}

	private Coalition getIntersection(Coalition c1, Coalition c2) {

		HashSet<String> intersection = (HashSet<String>) ((HashSet<String>) c1.getAgents()).clone();
		intersection.retainAll(c2.getAgents());
		if (intersection.isEmpty()) {
			return null;
		} else {
			return coalitions.get(intersection);
		}
	}

	public String getSubadditivityProof() {
		return subadditivityProof;
	}

	public String getSuperadditivityProof() {
		return superadditivityProof;
	}

	private Coalition getUnion(Coalition c1, Coalition c2) {
		HashSet<String> union = (HashSet<String>) ((HashSet<String>) c1.getAgents()).clone();
		union.addAll(c2.getAgents());
		return coalitions.get(union);
	}

	public boolean inCore(Map<String, Double> payoffs) {

		for (Coalition coalition : coalitions.values()) {
			if (addUpValues(payoffs.values()) < coalition.getValue()) {
				return false;
			}
		}
		return true;
	}

	public boolean isConvex() {
		return convex;
	}

	public boolean isSubadditiv() {
		return subadditiv;
	}

	public boolean isSuperadditiv() {
		return superadditiv;
	}

	// compute maximum surplus of agent agent1 over agent2 with respect to
	// payoff distribution
	private double maxSurplus(Map<String, Double> payoffs, String agent1, String agent2) {

		double maxSurplus = Double.NEGATIVE_INFINITY;

		for (Coalition coalition : coalitions.values()) {
			if (coalition.containsAgent(agent1) && !coalition.containsAgent(agent2)) {
				double excess = excess(coalition, payoffs);
				if (excess > maxSurplus) {
					maxSurplus = excess;
				}
			}
		}

		return maxSurplus;
	}
}
