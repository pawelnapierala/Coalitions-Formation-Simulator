package simulator;

public interface IController {

	void computeGameProperties();

	void loadGame();

	void nextStep();

	void pickCoalitionsFormationsGraphVertex(String vertex);

	void pickKnownAgentsGraphVertex(String vertex);

	void prevStep();

	void showCoalitionsFormations();

	void showFinalCoalitions();

	void showKnownAgentsGraph();

	void simulationFinished();

	void startSimulation();

	void unpickCoalitionsFormationsGraphVertex(String vertex);

	void unpickKnownAgentsGraphVertex(String vertex);

}
