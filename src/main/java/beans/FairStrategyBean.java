package beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ontology.Coalition;

public class FairStrategyBean extends StrategyBean {

	private Map<Coalition, Double> coalitionsAveragePayoffs = null;

	@Override
	protected HashMap<String, Double> analyseDemands() {

		Coalition coalition = world.getCoalition((Set<String>) world.getCurrentInvitation().getContent());

		double demandsSum = getMyDemand(null);
		for (double demand : world.getReceivedDemands().values()) {
			demandsSum += demand;
		}

		if (demandsSum <= coalition.getValue()) {
			// can fulfill all demands

			double rest = coalition.getValue() - demandsSum;
			double restPerAgent = rest / coalition.getSize();
			HashMap<String, Double> payoffs = new HashMap<String, Double>();

			for (String agent : coalition.getAgents()) {
				double agentPayoff = (world.getMyName().equals(agent) ? getMyDemand(null) : world.getReceivedDemands()
						.get(agent)) + restPerAgent;
				payoffs.put(agent, agentPayoff);
			}

			return payoffs;

		} else {
			return null;
		}
	}

	@Override
	public void doInit() throws Exception {

		super.doInit();

		coalitionsAveragePayoffs = new HashMap<Coalition, Double>();
	}

	@Override
	protected double getCoalitionRank(Coalition coalition) {

		return coalitionsAveragePayoffs.get(coalition);
	}

	@Override
	protected double getMyDemand(Coalition coalition) {
		return world.getMyPayoff() + 1;
	}

	@Override
	protected double getMyMinDemand() {
		return getMyDemand(null);
	}

	@Override
	protected void negotiationUnsuccessful() {
		// fair agent does nothing here
	}

	@Override
	protected void rankCoalitions() {

		for (Coalition coalition : world.getCoalitions()) {
			double averagePayoff = coalition.getValue() / coalition.getSize();
			coalitionsAveragePayoffs.put(coalition, averagePayoff);
		}
	}
}
