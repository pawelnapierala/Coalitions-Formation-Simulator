package simulator;

public class Main {

	public static void main(String[] args) {

		IModel model = new SimulatorModel();
		IController controller = new SimulatorController(model);
	}

}
