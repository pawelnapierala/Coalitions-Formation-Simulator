package beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ontology.Coalition;

public class AggressiveStrategyBean extends StrategyBean {

	private Map<Coalition, Double> coalitionsAveragePayoffs = null;
	private Map<Coalition, Double> demandMultipliers = null;

	@Override
	protected HashMap<String, Double> analyseDemands() {

		Coalition coalition = world.getCoalition((Set<String>) world.getCurrentInvitation().getContent());

		double demandsSum = 0;
		for (double demand : world.getReceivedDemands().values()) {
			demandsSum += demand;
		}

		if (demandsSum + getMyMinDemand() <= coalition.getValue()) {
			// can fulfill all demands

			double rest = coalition.getValue() - demandsSum;
			HashMap<String, Double> payoffs = new HashMap<String, Double>();

			for (String agent : coalition.getAgents()) {
				double agentPayoff = (world.getMyName().equals(agent) ? rest : world.getReceivedDemands().get(agent));
				payoffs.put(agent, agentPayoff);
			}

			return payoffs;

		} else {
			return null;
		}
	}

	private void decreaseDemandMultiplier(Coalition coalition) {

		double multiplier = demandMultipliers.get(coalition);

		if (multiplier > 1d) {
			demandMultipliers.put(coalition, multiplier - 0.5);
		}
	}

	@Override
	public void doInit() throws Exception {

		super.doInit();

		coalitionsAveragePayoffs = new HashMap<Coalition, Double>();
		demandMultipliers = new HashMap<Coalition, Double>();
	}

	@Override
	public void doStart() throws Exception {

		super.doStart();

		initDemandMultipliers();
	}

	@Override
	protected double getCoalitionRank(Coalition coalition) {

		return coalitionsAveragePayoffs.get(coalition) * demandMultipliers.get(coalition);
	}

	@Override
	protected double getMyDemand(Coalition coalition) {

		double myDemand = world.getMyPayoff() + 1;

		while (demandMultipliers.get(coalition) > 1d
				&& myDemand * demandMultipliers.get(coalition) >= coalition.getValue()) {
			decreaseDemandMultiplier(coalition);
		}

		return myDemand * demandMultipliers.get(coalition);
	}

	@Override
	protected double getMyMinDemand() {
		return world.getMyPayoff() + 1;
	}

	private void initDemandMultipliers() {

		for (Coalition coalition : world.getCoalitions()) {
			demandMultipliers.put(coalition, 2d);
		}
	}

	@Override
	protected void negotiationUnsuccessful() {

		decreaseDemandMultiplier(world.getCoalition((Set<String>) world.getCurrentInvitation().getContent()));
	}

	@Override
	protected void rankCoalitions() {

		for (Coalition coalition : world.getCoalitions()) {
			double averagePayoff = coalition.getValue() / coalition.getSize();
			coalitionsAveragePayoffs.put(coalition, averagePayoff);
		}
	}
}
