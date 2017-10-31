package simulator;

import java.io.File;

import javax.swing.JFileChooser;

public class SimulatorController implements IController {

	private IModel model;
	private SimulatorView view;

	public SimulatorController(IModel model) {
		this.model = model;

		view = new SimulatorView(this, model);
		view.initGUI();
		view.addControls();
	}

	@Override
	public void computeGameProperties() {
		view.disableComputePropertiesBtn();
		model.computeGameProperties();
		view.showGamePropertiesTab();
		view.switchToGamePropertiesTab();
	}

	public void goToStep(int stepNr) {
		int goToStep = stepNr;
		int currentStep = model.getCurrentStepNr();
		int lastStep = model.getNumberOfSteps() - 1;

		// checks if parameter is correct, i.e. stepNr >=0 and stepNr <=
		// lastStep
		if (stepNr == currentStep) {
			return;
		} else if (stepNr < 0) {
			goToStep = 0;
		} else if (stepNr > lastStep) {
			goToStep = lastStep;
		}

		if (goToStep == 0) {
			view.disablePrevStepButton();
		} else {
			view.enablePrevStepButton();
		}

		if (stepNr == lastStep) {
			view.disableNextStepButton();
		} else {
			view.enableNextStepButton();
		}

		model.goToStep(goToStep);
	}

	@Override
	public void loadGame() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(view);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			model.loadGameFromFile(file);
		}

		view.enableComputePropertiesBtn();
		view.enableStartSimulationBtn();
	}

	@Override
	public void nextStep() {
		int currentStep = model.getCurrentStepNr();
		goToStep(currentStep + 1);
	}

	@Override
	public void pickCoalitionsFormationsGraphVertex(String vertex) {
		model.pickCoalitionsFormationsGraphVertex(vertex);
		view.updateCoalitionsFormationsGraphAgentInfo();
	}

	@Override
	public void pickKnownAgentsGraphVertex(String vertex) {
		model.pickKnownAgentsGraphVertex(vertex);

		view.showInfoMsg();
		view.updateKnownAgentsGraphAgentInfo();
	}

	@Override
	public void prevStep() {
		int currentStep = model.getCurrentStepNr();
		goToStep(currentStep - 1);
	}

	@Override
	public void showCoalitionsFormations() {
		if (model.getCoalitionsFormationsGraph() == null) {
			model.computeCoalitionsFormations();
		}

		view.showCoalitionsFormationsGraphControl();
		view.showInfoMsg();
	}

	@Override
	public void showFinalCoalitions() {
		view.hideCoalitionsFormationsGraphControl();
		view.hideInfoMsg();
	}

	@Override
	public void showKnownAgentsGraph() {
		view.hideCoalitionsFormationsGraphControl();
		view.hideInfoMsg();

		if (model.getKnownAgentsGraph() == null) {
			model.computeFinalAgents();
			model.computeKnownAgentsGraph();
		}
	}

	@Override
	public void simulationFinished() {
		model.computeFinalCoalitions();

		view.showSimResultsTab();
		view.showKnownAgentsGraphTab();
		view.showCoalitionsFormationsTab();
		view.switchToSimResultsTab();
	}

	@Override
	public void startSimulation() {
		view.disableLoadGameBtn();
		view.disableStartSimulationBtn();
		model.startSimulation();
	}

	@Override
	public void unpickCoalitionsFormationsGraphVertex(String vertex) {
		model.unpickCoalitionsFormationsGraphVertex();
		view.updateStepInfo();
	}

	@Override
	public void unpickKnownAgentsGraphVertex(String vertex) {
		model.unpickKnownAgentsGraphVertex();
		view.hideInfoMsg();
	}
}
