package beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ontology.Agent;
import ontology.Coalition;
import ontology.Message;
import ontology.World;
import de.dailab.jiactng.agentcore.AbstractAgentBean;

public abstract class StrategyBean extends AbstractAgentBean {

	private String knownAgentsParam;
	private String coalitionsValuesParam;

	protected World world = null;

	// sorts nach coalition value
	protected void addPreferredCoalition(Coalition coalition) {

		int index = Collections.binarySearch(world.getPreferredCoalitions(), coalition);
		world.getPreferredCoalitions().add(-index - 1, coalition);
	}

	public void addPreferredCoalitions(Collection<String> agents) {

		Set<String> knownAgentsPlusSelf = new HashSet<String>(world.getKnownAgents());
		knownAgentsPlusSelf.add(world.getMyName());

		for (Coalition coalition : world.getCoalitions()) {

			searchForNewCoalition: if (knownAgentsPlusSelf.containsAll(coalition.getAgents())) {

				for (String agent : agents) {
					if (coalition.containsAgent(agent)) {
						if (coalition.getValue() > world.getSingleCoalitionPayoff()) {
							addPreferredCoalition(coalition);
						}
						break searchForNewCoalition;
					}
				}
			}
		}
	}

	protected abstract HashMap<String, Double> analyseDemands();

	@Override
	public void doStart() throws Exception {

		super.doStart();

		parseKnownAgents();
		parseCoalitions();
		rankCoalitions();
		addPreferredCoalitions(world.getKnownAgents());
	}

	protected abstract double getCoalitionRank(Coalition coalition);

	public String getCoalitionsValuesParam() {
		return coalitionsValuesParam;
	}

	public String getKnownAgentsParam() {
		return knownAgentsParam;
	}

	protected abstract double getMyDemand(Coalition coalition);

	protected abstract double getMyMinDemand();

	public Coalition getPreferredCoalition() {

		Coalition preferredCoalition = null;
		double preferredCoalitionRank = 0;
		double myMinDemand = getMyMinDemand();

		for (Coalition coalition : world.getPreferredCoalitions()) {
			if (coalition.getValue() > myMinDemand && !coalition.equals(world.getMyCoalition())) {
				if (getCoalitionRank(coalition) > preferredCoalitionRank) {
					preferredCoalition = coalition;
					preferredCoalitionRank = getCoalitionRank(coalition);
				}
			} else {
				break;
			}
		}

		if (preferredCoalition == null && world.getSingleCoalitionPayoff() > world.getMyPayoff()) {
			preferredCoalition = world.getSingleCoalition();
		}

		return preferredCoalition;
	}

	public Agent getSelf() {
		return world.getSelf();
	}

	public boolean isInvitationAcceptable(Message invitation) {
		return getMyDemand(world.getCoalition((Set<String>) invitation.getContent())) < world.getCoalition(
				(Set<String>) invitation.getContent()).getValue();
	}

	protected abstract void negotiationUnsuccessful();

	private void parseCoalitions() {

		String[] coalitions = coalitionsValuesParam.split(";");

		for (String coalition : coalitions) {

			String[] value = coalition.split("=");
			String[] agents = value[0].split(",");
			HashSet<String> agentsSet = new HashSet<String>(Arrays.asList(agents));
			Coalition newCoalition = new Coalition(Double.parseDouble(value[1]), agentsSet);
			world.addCoalition(newCoalition);

			if (agents.length == 1 && agents[0].equals(world.getMyName())) {

				world.setMyCoalition(newCoalition);
				world.setSingleCoalition(newCoalition);
			}
		}
	}

	private void parseKnownAgents() {

		if (!knownAgentsParam.isEmpty()) {
			String[] agents = knownAgentsParam.split(",");
			for (String agent : agents) {
				world.addAgent(agent);
			}
		}
	}

	protected abstract void rankCoalitions();

	public void setCoalitionsValuesParam(String coalitions) {
		this.coalitionsValuesParam = coalitions;
	}

	public void setKnownAgentsParam(String agents) {
		this.knownAgentsParam = agents;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}
