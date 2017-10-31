package simulator;

import java.io.File;
import java.util.List;
import java.util.Set;

import ontology.Agent;
import ontology.Coalition;
import ontology.LogInfo;
import ontology.Step;
import edu.uci.ics.jung.graph.Graph;
import game.Game;

public interface IModel {

	void computeCoalitionsFormations();

	void computeFinalAgents();

	void computeFinalCoalitions();

	void computeGameProperties();

	void computeKnownAgentsGraph();

	Graph<String, Object> getCoalitionsFormationsGraph();

	Agent getCoalitionsFormationsGraphAgent(String agent);

	String getCoalitionsFormationsGraphPickedVertex();

	Set<String> getCurrentlyKnownAgents(String agent);

	Step getCurrentStep();

	LogInfo getCurrentStepInfo();

	int getCurrentStepNr();

	List<Coalition> getFinalCoalitions();

	Set<String> getFinalKnownAgents(String vertex);

	Game getGame();

	Graph getKnownAgentsGraph();

	Agent getKnownAgentsGraphAgent(String coalitionsFormationsGraphPickedVertex);

	String getKnownAgentsGraphPickedVertex();

	int getNumberOfSteps();

	void goToStep(int i);

	boolean isSimulationFinished();

	boolean isSimulationResultInCore();

	void loadGameFromFile(File file);

	void pickCoalitionsFormationsGraphVertex(String vertex);

	void pickKnownAgentsGraphVertex(String vertex);

	void registerObserver(IObserver iObserver);

	void startSimulation();

	void unpickCoalitionsFormationsGraphVertex();

	void unpickKnownAgentsGraphVertex();

}
