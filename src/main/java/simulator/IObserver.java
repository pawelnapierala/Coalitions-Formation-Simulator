package simulator;

public interface IObserver {

	void updateCoalitionsFormationsGraph();

	void updateCoalitionsFormationsGraphPickedVertex();

	void updateFinalAgents();

	void updateFinalCoalitions();

	void updateGameProperties();

	void updateKnownAgentsGraph();

	void updateKnownAgentsGraphPickedVertex();

	void updateSimulationFinished();

	void updateStepInfo();

	void updateStepNr();

}
