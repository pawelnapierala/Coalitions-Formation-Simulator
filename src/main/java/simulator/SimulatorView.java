package simulator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ontology.Agent;
import ontology.Coalition;
import ontology.MessageEdge;
import ontology.Step;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.Animator;

public class SimulatorView extends JFrame implements IObserver {

	private class EdgeColorTransformer<V, E> implements Transformer {

		private final Color COALITION_EDGE_COLOR = new Color(130, 130, 130);
		private final Color currentMessageColor;
		private final Color normalColor;
		private final Color changedColor;
		private Set<Object> edgesWithChangedColor = null;
		private Set<MessageEdge> currentMessageEdges = null;

		public EdgeColorTransformer(Color normalColor, Color changedColor, Color currentMessageColor) {
			this.normalColor = normalColor;
			this.changedColor = changedColor;
			this.currentMessageColor = currentMessageColor;
		}

		public void reset() {
			edgesWithChangedColor = null;
		}

		public void setCurrentMessageEdges(Set currentMessageEdges) {
			this.currentMessageEdges = currentMessageEdges;
		}

		public void setEdgesWithChangedColor(Set edgesWithChangedColor) {
			this.edgesWithChangedColor = edgesWithChangedColor;
		}

		@Override
		public Paint transform(Object edge) {

			if (currentMessageEdges != null && edge instanceof MessageEdge && currentMessageEdges.contains(edge)) {
				return currentMessageColor;
			}

			if (edgesWithChangedColor != null) {
				if (!(edgesWithChangedColor.contains(edge))) {
					return changedColor;
				}
			}

			if (edge instanceof String) {
				return COALITION_EDGE_COLOR;
			}

			return normalColor;
		}
	}

	private class EdgeShapeTransformer<V, E> implements Transformer {

		private final AbstractEdgeShapeTransformer bentLine = new EdgeShape.BentLine();
		private final AbstractEdgeShapeTransformer line = new EdgeShape.Line();

		@Override
		public Shape transform(Object edge) {
			if (edge instanceof MessageEdge) {
				return (new Line2D.Double());
			} else {
				return (new Line2D.Double());
			}
		}

	};

	private class EdgeStrokeTransformer<V, E> implements Transformer {

		private final float dash[] = { 3.0f };
		private final Stroke dashedLine = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				dash, 0.0f);
		private final Stroke solidLine = new BasicStroke();

		@Override
		public Stroke transform(Object edge) {
			if (edge instanceof MessageEdge && !((MessageEdge) edge).isReceived()) {
				return dashedLine;
			}
			return solidLine;
		}
	};

	class PickingGraphMousePluginTest extends PickingGraphMousePlugin<String, Object> {

		@Override
		protected void pickContainedVertices(VisualizationViewer<String, Object> arg0, Point2D arg1, Point2D arg2,
				boolean arg3) {

		}

	}

	private class VertexColorTransformer<V, E> implements Transformer<String, Paint> {

		private final Color NORMAL_COLOR;
		private final Color CHANGED_COLOR;

		private String selectedVertex = null;
		private Set<String> verticesWithChangedColor = null;

		public VertexColorTransformer(Color normalColor, Color changedColor) {
			NORMAL_COLOR = normalColor;
			CHANGED_COLOR = changedColor;
		}

		public void reset() {
			selectedVertex = null;
			verticesWithChangedColor = null;
		}

		public void setSelectedVertex(String selectedVertex) {
			this.selectedVertex = selectedVertex;
		}

		public void setVerticesWithChangedColor(Set<String> verticesWithChangedColor) {
			this.verticesWithChangedColor = verticesWithChangedColor;
		}

		@Override
		public Paint transform(String vertex) {

			if (selectedVertex != null) {
				if (!(verticesWithChangedColor.contains(vertex) || vertex.equals(selectedVertex))) {
					return CHANGED_COLOR;
				}
			}

			return NORMAL_COLOR;
		}
	}

	private final static class VertexStroke<V, E> implements Transformer<V, Stroke> {

		@Override
		public Stroke transform(V v) {
			return new BasicStroke(2);
		}
	}

	private static final long serialVersionUID = 1272472787243419028L;

	protected final static String CARD_COALITIONS_GRAPH = "Card with coalitions graph";
	protected final static String CARD_SIMULATION_RESULT = "Card with simulation's result";
	private final static String LBL_KNOWN_AGENTS_GRAPH = "Known agents graph";
	private final static String LBL_FINAL_COALITIONS = "Coalitions";
	private final static Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
	private final static Color VERTEX_COLOR = new Color(90, 120, 170);
	private final static Color VERTEX_BORDER_COLOR = new Color(0, 0, 0);

	private final static Color EDGE_COLOR = new Color(0, 0, 0);
	private final static Color VERTEX_COLOR_UNKNOWN_AGENT = new Color(200, 200, 200);
	private final static Color CURRENT_MESSAGE_EDGE_COLOR = new Color(30, 110, 0);

	private final static Color EDGE_COLOR_UNKNOWN_AGENT = TRANSPARENT_COLOR;
	private final static int VERTEX_BORDER = 3;

	private final static int DECIMAL_PLACES = 4;
	private final static String nl = System.getProperty("line.separator");
	IModel model;
	IController controller;

	Layout<String, Object> coalitionsFormationsGraphLayout = null;
	VisualizationViewer<String, Object> coalitionsFormationsGraphVV = null;
	VertexColorTransformer<String, Paint> coalitionsFormationsGraphVertexColor = null;
	EdgeColorTransformer<Object, Paint> coalitionsFormationsGraphEdgeColor = null;
	EdgeStrokeTransformer<Object, Stroke> coalitionsFormationsGraphEdgeStroke = null;
	EdgeShapeTransformer<Object, Shape> coalitionsFormationsGraphEdgeShape = null;

	Graph<String, String> knownAgentsGraph = null;
	VisualizationViewer<String, String> knownAgentsGraphVV = null;

	VertexColorTransformer<String, Paint> knownAgentsGraphVertexColor = null;
	EdgeColorTransformer<String, Paint> knownAgentsGraphEdgeColor = null;
	private String visibleCard = null;
	private ItemListener edgeItemListener = null;
	private JPanel mainPane;
	private JButton btnStartSimulation;
	private JButton btnPrevstep;
	private JButton btnNextstep;
	private JLabel lblStep;
	private JLabel lblFinalCoalitions;
	private JLabel lblknownAgentsGraph;
	private JScrollPane scrollPane;
	private JPanel centerPanel;
	private JButton btnLoadGame;
	private JTextArea txtrInfoMsg;
	private JPanel coalitionsFormationsTab;
	JPanel coalitionsFormationsGraphControlPanel;
	private Box vBoxSimResult;
	private JScrollPane simResultsTab;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JPanel buttonsPanel;
	private Component verticalStrut;
	private JScrollPane scrollPaneInfoMsg;
	JPanel infoMsgPanel;

	private JPanel sidePanel;

	private JSpinner spinner;

	private JTabbedPane tabsPane;

	private JPanel knownAgentsGraphTab;

	boolean coalitionsFormationsGraphInitialized = false;
	private Component verticalStrut_1;
	private JButton btnComputeProperties;
	private JPanel gamePropertiesTab;
	private JTextArea txtrGameProperties;

	public SimulatorView(IController controller, IModel model) {
		this.controller = controller;
		this.model = model;
		model.registerObserver(this);
		initGUI();
	}

	public void addControls() {
		btnStartSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.startSimulation();
			}
		});

		btnPrevstep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.prevStep();
			}
		});

		btnNextstep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.nextStep();
			}
		});

		btnLoadGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.loadGame();
			}
		});

		btnComputeProperties.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.computeGameProperties();
			}
		});

		tabsPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) event.getSource();
				int selectedIndex = sourceTabbedPane.getSelectedIndex();

				if (selectedIndex != -1) {
					switch (tabsPane.getTitleAt(selectedIndex)) {
					case "Simulation results":
						controller.showFinalCoalitions();
						break;

					case "Known agents graph":
						controller.showKnownAgentsGraph();
						break;

					case "Coalitions formations":
						controller.showCoalitionsFormations();
						break;
					}
				}
			}
		});
	}

	public void clearInfoMsg() {
		txtrInfoMsg.setText("");
	}

	public void disableComputePropertiesBtn() {
		btnComputeProperties.setEnabled(false);
	}

	public void disableLoadGameBtn() {
		btnLoadGame.setEnabled(false);
	}

	public void disableNextStepButton() {
		btnNextstep.setEnabled(false);
	}

	public void disablePrevStepButton() {
		btnPrevstep.setEnabled(false);
	}

	public void disableStartSimulationBtn() {
		btnStartSimulation.setEnabled(false);
	}

	public void enableComputePropertiesBtn() {
		btnComputeProperties.setEnabled(true);
	}

	public void enableNextStepButton() {
		btnNextstep.setEnabled(true);
	};

	public void enablePrevStepButton() {
		btnPrevstep.setEnabled(true);
	};

	public void enableStartSimulationBtn() {
		btnStartSimulation.setEnabled(true);
	}

	private String formatDouble(double number) {
		return String.format("%." + DECIMAL_PLACES + "f", number);
	};

	private String formatKnownAgents(Collection<String> agents) {
		StringBuilder strB = new StringBuilder();

		Iterator<String> i = agents.iterator();
		while (i.hasNext()) {
			strB.append(i.next());
			strB.append(i.hasNext() ? ", " : "");
		}

		return strB.toString();
	}

	private String formatPayoffs(Map<String, Double> payoffs) {
		StringBuilder strB = new StringBuilder();

		for (Map.Entry<String, Double> entry : payoffs.entrySet()) {
			strB.append(entry.getKey());
			strB.append(" = ");
			strB.append(formatDouble(entry.getValue()));
			strB.append(nl);
		}

		return strB.toString();
	}

	public void hideCoalitionsFormationsGraphControl() {
		coalitionsFormationsGraphControlPanel.setVisible(false);
	}

	public void hideCoalitionsFormationsTab() {
		tabsPane.remove(coalitionsFormationsTab);
	}

	public void hideInfoMsg() {
		infoMsgPanel.setVisible(false);
	}

	public void hideKnownAgentsGraphTab() {
		tabsPane.remove(knownAgentsGraphTab);
	}

	public void hideSimResultsTab() {
		tabsPane.remove(simResultsTab);
	}

	private void initCoalitionsFormationsGraph() {
		Dimension dimension = new Dimension(centerPanel.getSize().width - 50, centerPanel.getSize().height - 50);

		coalitionsFormationsGraphLayout = new FRLayout<String, Object>(model.getCoalitionsFormationsGraph());
		coalitionsFormationsGraphLayout.setSize(dimension);

		PickingGraphMousePlugin<String, Object> pickGr = new PickingGraphMousePluginTest();
		pickGr.setLensColor(TRANSPARENT_COLOR);

		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(pickGr);
		gm.add(new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK));
		gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f));

		coalitionsFormationsGraphVertexColor = new VertexColorTransformer<String, Paint>(VERTEX_COLOR,
				VERTEX_COLOR_UNKNOWN_AGENT);
		coalitionsFormationsGraphEdgeColor = new EdgeColorTransformer<Object, Paint>(EDGE_COLOR,
				EDGE_COLOR_UNKNOWN_AGENT, CURRENT_MESSAGE_EDGE_COLOR);
		coalitionsFormationsGraphEdgeStroke = new EdgeStrokeTransformer<Object, Stroke>();
		coalitionsFormationsGraphEdgeShape = new EdgeShapeTransformer<Object, Shape>();

		Transformer<String, Shape> vertexShape = new Transformer<String, Shape>() {
			@Override
			public Shape transform(String s) {
				return new Ellipse2D.Double(-40, -15, 80, 30);
			}
		};

		coalitionsFormationsGraphVV = new VisualizationViewer<String, Object>(coalitionsFormationsGraphLayout);
		coalitionsFormationsGraphVV.setPreferredSize(dimension);
		coalitionsFormationsGraphVV.setBackground(new Color(255, 255, 255));
		coalitionsFormationsGraphVV.getRenderContext().setVertexFillPaintTransformer(
				coalitionsFormationsGraphVertexColor);
		coalitionsFormationsGraphVV.getRenderContext().setEdgeDrawPaintTransformer(coalitionsFormationsGraphEdgeColor);
		coalitionsFormationsGraphVV.getRenderContext().setEdgeStrokeTransformer(coalitionsFormationsGraphEdgeStroke);
		coalitionsFormationsGraphVV.getRenderContext().setArrowDrawPaintTransformer(coalitionsFormationsGraphEdgeColor);
		coalitionsFormationsGraphVV.getRenderContext().setArrowFillPaintTransformer(coalitionsFormationsGraphEdgeColor);
		coalitionsFormationsGraphVV.getRenderContext().setVertexShapeTransformer(vertexShape);
		coalitionsFormationsGraphVV.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		coalitionsFormationsGraphVV.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		coalitionsFormationsGraphVV.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve());
		coalitionsFormationsGraphVV.setGraphMouse(gm);

		AbstractEdgeShapeTransformer<String, Object> aesf = (AbstractEdgeShapeTransformer<String, Object>) coalitionsFormationsGraphVV
				.getRenderContext().getEdgeShapeTransformer();
		aesf.setControlOffsetIncrement(50);

		coalitionsFormationsGraphVV.getPickedVertexState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				String sourceVertex = (String) event.getItem();
				boolean isPicked = coalitionsFormationsGraphVV.getPickedVertexState().isPicked(sourceVertex);

				if (isPicked) {
					controller.pickCoalitionsFormationsGraphVertex(sourceVertex);
				} else {
					controller.unpickCoalitionsFormationsGraphVertex(sourceVertex);
				}
			}
		});

		coalitionsFormationsTab.add(coalitionsFormationsGraphVV, BorderLayout.CENTER);
		coalitionsFormationsTab.revalidate();
		validate();
	}

	public void initGUI() {
		setTitle("Coalitions Formation Simulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		mainPane.setLayout(new BorderLayout(0, 0));

		sidePanel = new JPanel();
		mainPane.add(sidePanel, BorderLayout.WEST);
		GridBagLayout gbl_sidePanel = new GridBagLayout();
		gbl_sidePanel.columnWidths = new int[] { 0, 0 };
		gbl_sidePanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_sidePanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_sidePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		sidePanel.setLayout(gbl_sidePanel);

		buttonsPanel = new JPanel();
		buttonsPanel.setPreferredSize(new Dimension(200, 90));
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.insets = new Insets(0, 0, 10, 0);
		gbc_buttonsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonsPanel.gridx = 0;
		gbc_buttonsPanel.gridy = 0;
		sidePanel.add(buttonsPanel, gbc_buttonsPanel);
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

		btnLoadGame = new JButton("Load game");
		btnLoadGame.setMaximumSize(new Dimension(180, 25));
		btnLoadGame.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnLoadGame);
		btnLoadGame.setActionCommand("load game");

		verticalStrut = Box.createVerticalStrut(5);
		buttonsPanel.add(verticalStrut);

		btnStartSimulation = new JButton("Start simulation");
		btnStartSimulation.setEnabled(false);
		btnStartSimulation.setMaximumSize(new Dimension(180, 25));
		btnStartSimulation.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnStartSimulation);
		btnStartSimulation.setActionCommand("start simulation");

		verticalStrut_1 = Box.createVerticalStrut(5);
		buttonsPanel.add(verticalStrut_1);

		btnComputeProperties = new JButton("Compute properties");
		btnComputeProperties.setEnabled(false);
		btnComputeProperties.setMaximumSize(new Dimension(180, 25));
		btnComputeProperties.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnComputeProperties);

		coalitionsFormationsGraphControlPanel = new JPanel();
		coalitionsFormationsGraphControlPanel.setVisible(false);
		coalitionsFormationsGraphControlPanel.setMinimumSize(new Dimension(10, 30));
		coalitionsFormationsGraphControlPanel.setPreferredSize(new Dimension(10, 25));
		GridBagConstraints gbc_coalitionsGraphControlPanel = new GridBagConstraints();
		gbc_coalitionsGraphControlPanel.insets = new Insets(0, 0, 10, 0);
		gbc_coalitionsGraphControlPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_coalitionsGraphControlPanel.gridx = 0;
		gbc_coalitionsGraphControlPanel.gridy = 2;
		sidePanel.add(coalitionsFormationsGraphControlPanel, gbc_coalitionsGraphControlPanel);
		coalitionsFormationsGraphControlPanel.setLayout(new GridLayout(0, 3, 0, 0));

		btnPrevstep = new JButton("<");
		btnPrevstep.setEnabled(false);
		coalitionsFormationsGraphControlPanel.add(btnPrevstep);
		btnPrevstep.setActionCommand("prev step");

		lblStep = new JLabel("0");
		lblStep.setHorizontalAlignment(SwingConstants.CENTER);
		coalitionsFormationsGraphControlPanel.add(lblStep);

		btnNextstep = new JButton(">");
		coalitionsFormationsGraphControlPanel.add(btnNextstep);
		btnNextstep.setActionCommand("next step");

		infoMsgPanel = new JPanel();
		infoMsgPanel.setBorder(null);
		infoMsgPanel.setVisible(false);
		GridBagConstraints gbc_infoMsgPanel = new GridBagConstraints();
		gbc_infoMsgPanel.insets = new Insets(5, 0, 0, 0);
		gbc_infoMsgPanel.fill = GridBagConstraints.BOTH;
		gbc_infoMsgPanel.gridx = 0;
		gbc_infoMsgPanel.gridy = 3;
		sidePanel.add(infoMsgPanel, gbc_infoMsgPanel);
		infoMsgPanel.setLayout(new BorderLayout(0, 0));

		txtrInfoMsg = new JTextArea();
		txtrInfoMsg.setBorder(null);
		txtrInfoMsg.setBounds(0, 32, 200, 165);
		txtrInfoMsg.setWrapStyleWord(true);
		txtrInfoMsg.setLineWrap(true);
		txtrInfoMsg.setEditable(false);

		scrollPaneInfoMsg = new JScrollPane(txtrInfoMsg);
		scrollPaneInfoMsg.setBorder(null);
		infoMsgPanel.add(scrollPaneInfoMsg);

		centerPanel = new JPanel();
		centerPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
		mainPane.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));

		tabsPane = new JTabbedPane(SwingConstants.TOP);
		centerPanel.add(tabsPane);

		gamePropertiesTab = new JPanel();
		gamePropertiesTab.setLayout(new BorderLayout(0, 0));

		txtrGameProperties = new JTextArea();
		JScrollPane scrollPaneGameProperties = new JScrollPane(txtrGameProperties);
		gamePropertiesTab.add(scrollPaneGameProperties, BorderLayout.CENTER);

		vBoxSimResult = Box.createVerticalBox();

		simResultsTab = new JScrollPane(vBoxSimResult);
		simResultsTab.setBorder(null);
		simResultsTab.getViewport().setBackground(Color.WHITE);

		knownAgentsGraphTab = new JPanel();
		knownAgentsGraphTab.setLayout(new BorderLayout(0, 0));

		coalitionsFormationsTab = new JPanel();
		coalitionsFormationsTab.setLayout(new BorderLayout(0, 0));

		setVisible(true);
	}

	public boolean isCoalitionsFormationsGraphControlVisible() {
		return coalitionsFormationsGraphControlPanel.isVisible();
	}

	public boolean isInfoMsgVisible() {
		return infoMsgPanel.isVisible();
	}

	private void printAgentInfo(Agent agent) {
		txtrInfoMsg.setText("NAME: " + agent.getName() + nl + nl);
		txtrInfoMsg.append("PAYOFF: " + formatDouble(agent.getPayoff()) + nl + nl);
		txtrInfoMsg.append("COALITION: (" + formatDouble(agent.getCoalition().getValue()) + ")" + nl);
		txtrInfoMsg.append(formatPayoffs(agent.getCoalition().getPayoffs()) + nl);
		txtrInfoMsg.append("KNOWN AGENTS: " + nl);
		txtrInfoMsg.append(formatKnownAgents(agent.getKnownAgents()));
	}

	public void showCoalitionsFormationsGraphControl() {
		coalitionsFormationsGraphControlPanel.setVisible(true);
	}

	public void showCoalitionsFormationsTab() {
		tabsPane.addTab("Coalitions formations", coalitionsFormationsTab);
	}

	public void showGamePropertiesTab() {
		tabsPane.addTab("Game properties", gamePropertiesTab);
	}

	public void showInfoMsg() {
		infoMsgPanel.setVisible(true);
	}

	public void showKnownAgentsGraphTab() {
		tabsPane.addTab("Known agents graph", knownAgentsGraphTab);
	}

	public void showSimResultsTab() {
		tabsPane.addTab("Simulation results", simResultsTab);
	}

	public void switchToGamePropertiesTab() {
		tabsPane.setSelectedComponent(gamePropertiesTab);
	}

	public void switchToSimResultsTab() {
		tabsPane.setSelectedComponent(simResultsTab);
	}

	@Override
	public void updateCoalitionsFormationsGraph() {

		if (coalitionsFormationsGraphVV == null) {
			initCoalitionsFormationsGraph();
		}

		// color current message
		if (model.getCurrentStep().getReceivedMessage() != null) {
			Set<MessageEdge> receivedMessage = new HashSet<MessageEdge>();
			receivedMessage.add(model.getCurrentStep().getReceivedMessage());
			coalitionsFormationsGraphEdgeColor.setCurrentMessageEdges(receivedMessage);
		} else {
			coalitionsFormationsGraphEdgeColor.setCurrentMessageEdges(model.getCurrentStep().getAddedMessages());
		}

		coalitionsFormationsGraphLayout.initialize();
		Relaxer relaxer = new VisRunner((IterativeContext) coalitionsFormationsGraphLayout);
		relaxer.stop();
		relaxer.prerelax();
		StaticLayout<String, Object> staticLayout = new StaticLayout<String, Object>(
				model.getCoalitionsFormationsGraph(), coalitionsFormationsGraphLayout);
		LayoutTransition<String, Object> lt = new LayoutTransition<String, Object>(coalitionsFormationsGraphVV,
				coalitionsFormationsGraphVV.getGraphLayout(), staticLayout);
		Animator animator = new Animator(lt);
		animator.start();
		coalitionsFormationsGraphVV.repaint();
	}

	public void updateCoalitionsFormationsGraphAgentInfo() {
		Agent agent = model.getCoalitionsFormationsGraphAgent(model.getCoalitionsFormationsGraphPickedVertex());
		printAgentInfo(agent);
	}

	@Override
	public void updateCoalitionsFormationsGraphPickedVertex() {
		String vertex = model.getCoalitionsFormationsGraphPickedVertex();

		if (vertex == null) {
			coalitionsFormationsGraphVertexColor.reset();
			coalitionsFormationsGraphEdgeColor.reset();

		} else {
			coalitionsFormationsGraphVertexColor.setSelectedVertex(vertex);
			coalitionsFormationsGraphVertexColor.setVerticesWithChangedColor(model.getCurrentlyKnownAgents(vertex));

			Graph<String, Object> graph = model.getCoalitionsFormationsGraph();
			Set<Object> coalitionEdges = new HashSet<Object>();
			for (String neighbour : graph.getNeighbors(vertex)) {
				coalitionEdges.addAll(graph.getOutEdges(neighbour));
			}
			coalitionsFormationsGraphEdgeColor.setEdgesWithChangedColor(coalitionEdges);
		}
	}

	@Override
	public void updateFinalAgents() {
	}

	@Override
	public void updateFinalCoalitions() {
		List<Coalition> coalitions = model.getFinalCoalitions();

		vBoxSimResult.add(Box.createRigidArea(new Dimension(0, 20)));

		lblFinalCoalitions = new JLabel(LBL_FINAL_COALITIONS);
		lblFinalCoalitions.setFont(new Font("Dialog", Font.BOLD, 18));
		vBoxSimResult.add(lblFinalCoalitions);

		vBoxSimResult.add(Box.createRigidArea(new Dimension(0, 20)));

		for (Coalition coalition : coalitions) {
			String headers[] = new String[coalition.getSize() + 2];
			headers[0] = "";
			headers[1] = "";

			String data[][] = new String[2][coalition.getSize() + 2];
			data[0][0] = "Size";
			data[1][0] = String.valueOf(coalition.getSize());
			data[0][1] = "Value";
			data[1][1] = formatDouble(coalition.getValue());

			List<String> sortedPayoffsKeys = new ArrayList<String>(coalition.getPayoffs().keySet());
			Collections.sort(sortedPayoffsKeys);
			for (int i = 0; i < sortedPayoffsKeys.size(); i++) {
				headers[i + 2] = "";
				data[0][i + 2] = sortedPayoffsKeys.get(i);
				data[1][i + 2] = formatDouble(coalition.getPayoffs().get(sortedPayoffsKeys.get(i)));
			}

			JTable coalitionTable = new JTable(data, headers) {

				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
					Component c = super.prepareRenderer(renderer, row, column);
					JComponent jc = (JComponent) c;

					if (row == 0 && column == 0) {
						jc.setBorder(new MatteBorder(1, 1, 0, 0, Color.BLACK));
					} else if (column == 0) {
						jc.setBorder(new MatteBorder(0, 1, 0, 0, Color.BLACK));
					} else if (row == 0) {
						jc.setBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK));
					}

					return c;
				}
			};

			coalitionTable.setGridColor(Color.BLACK);
			coalitionTable.setRowHeight(25);
			coalitionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			coalitionTable.setColumnSelectionAllowed(true);

			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setHorizontalAlignment(SwingConstants.CENTER);
			renderer.setVerticalAlignment(SwingConstants.CENTER);

			for (int c = 0; c < coalitionTable.getColumnCount(); c++) {
				TableColumn column = coalitionTable.getColumnModel().getColumn(c);
				column.setCellRenderer(renderer);
				column.setPreferredWidth(100);
			}

			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new BorderLayout(0, 0));
			tablePanel.add(coalitionTable, BorderLayout.WEST);
			tablePanel.setBorder(new EmptyBorder(0, 20, 20, 20));
			tablePanel.setBackground(Color.WHITE);

			vBoxSimResult.add(tablePanel);
		}

		vBoxSimResult.revalidate();
		validate();
	}

	@Override
	public void updateGameProperties() {

		// superadditiv? subadditiv? convex?
		txtrGameProperties.setText("Superadditiv: " + model.getGame().isSuperadditiv()
				+ (model.getGame().isSuperadditiv() ? "" : nl + model.getGame().getSuperadditivityProof()) + nl + nl);
		txtrGameProperties.append("Subadditiv: " + model.getGame().isSubadditiv()
				+ (model.getGame().isSubadditiv() ? "" : nl + model.getGame().getSubadditivityProof()) + nl + nl);
		txtrGameProperties.append("Convex: " + model.getGame().isConvex()
				+ (model.getGame().isConvex() ? "" : nl + model.getGame().getConvexityProof()) + nl + nl);

		// result in core?
		if (model.isSimulationFinished()) {
			txtrGameProperties.append("Simulation result in core: " + (model.isSimulationResultInCore() ? "yes" : "no")
					+ nl + nl);
		}

		// kernel
		if (model.isSimulationFinished()) {
			txtrGameProperties.append("Kernel: " + nl
					+ formatPayoffs(model.getGame().computeKernel(model.getFinalCoalitions())) + nl + nl);
		}

		// shapley values
		txtrGameProperties.append("Shapley values: " + nl + formatPayoffs(model.getGame().computeShapleyValue()) + nl
				+ nl);

		// best coalition structure
		txtrGameProperties.append("Best coalition structure: " + nl);
		for (Coalition coalition : model.getGame().computeBestCoalitionStructure()) {
			txtrGameProperties.append(coalition.getAgents() + nl);
		}
	}

	@Override
	public void updateKnownAgentsGraph() {
		Dimension dimension = new Dimension(400, 400);

		Layout<String, String> knownAgentsGraphLayout = new KKLayout<String, String>(model.getKnownAgentsGraph());
		knownAgentsGraphLayout.setSize(dimension);

		PickingGraphMousePlugin<String, Object> pickGr = new PickingGraphMousePluginTest();
		pickGr.setLensColor(TRANSPARENT_COLOR);

		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(pickGr);
		gm.add(new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK));
		gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f));

		knownAgentsGraphVertexColor = new VertexColorTransformer<String, Paint>(VERTEX_COLOR,
				VERTEX_COLOR_UNKNOWN_AGENT);
		knownAgentsGraphEdgeColor = new EdgeColorTransformer<String, Paint>(EDGE_COLOR, EDGE_COLOR_UNKNOWN_AGENT,
				CURRENT_MESSAGE_EDGE_COLOR);

		Transformer<String, Paint> vertexPaintBorder = new Transformer<String, Paint>() {
			@Override
			public Paint transform(String s) {
				return new Color(90, 120, 170, 100);
			}
		};

		Transformer<String, Shape> vertexShape = new Transformer<String, Shape>() {
			@Override
			public Shape transform(String s) {
				Ellipse2D rect = new Ellipse2D.Double(-40, -15, 80, 30);
				return rect;
			}
		};

		Transformer<String, Stroke> vertexStroke = new VertexStroke<String, Stroke>();

		knownAgentsGraphVV = new VisualizationViewer<String, String>(knownAgentsGraphLayout);
		knownAgentsGraphVV.setPreferredSize(dimension);
		knownAgentsGraphVV.setBackground(new Color(255, 255, 255));
		knownAgentsGraphVV.getRenderContext().setVertexFillPaintTransformer(knownAgentsGraphVertexColor);
		knownAgentsGraphVV.getRenderContext().setEdgeDrawPaintTransformer(knownAgentsGraphEdgeColor);
		knownAgentsGraphVV.getRenderContext().setArrowDrawPaintTransformer(knownAgentsGraphEdgeColor);
		knownAgentsGraphVV.getRenderContext().setArrowFillPaintTransformer(knownAgentsGraphEdgeColor);
		knownAgentsGraphVV.getRenderContext().setVertexDrawPaintTransformer(vertexPaintBorder);
		knownAgentsGraphVV.getRenderContext().setVertexShapeTransformer(vertexShape);
		knownAgentsGraphVV.getRenderContext().setVertexStrokeTransformer(vertexStroke);
		knownAgentsGraphVV.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		knownAgentsGraphVV.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		knownAgentsGraphVV.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<String, String>());
		knownAgentsGraphVV.setGraphMouse(gm);

		knownAgentsGraphVV.getPickedVertexState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				String sourceVertex = (String) event.getItem();
				boolean isPicked = knownAgentsGraphVV.getPickedVertexState().isPicked(sourceVertex);

				if (isPicked) {
					controller.pickKnownAgentsGraphVertex(sourceVertex);
				} else {
					controller.unpickKnownAgentsGraphVertex(sourceVertex);
				}
			}
		});

		knownAgentsGraphTab.add(Box.createRigidArea(new Dimension(0, 10)));

		lblknownAgentsGraph = new JLabel(LBL_KNOWN_AGENTS_GRAPH);
		lblknownAgentsGraph.setFont(new Font("Dialog", Font.BOLD, 18));
		knownAgentsGraphTab.add(lblknownAgentsGraph);

		knownAgentsGraphTab.add(Box.createRigidArea(new Dimension(0, 10)));
		knownAgentsGraphTab.add(knownAgentsGraphVV);
		knownAgentsGraphTab.revalidate();
		validate();
	}

	public void updateKnownAgentsGraphAgentInfo() {
		Agent agent = model.getKnownAgentsGraphAgent(model.getKnownAgentsGraphPickedVertex());
		printAgentInfo(agent);
	}

	@Override
	public void updateKnownAgentsGraphPickedVertex() {
		String vertex = model.getKnownAgentsGraphPickedVertex();

		if (vertex == null) {
			knownAgentsGraphVertexColor.reset();
			knownAgentsGraphEdgeColor.reset();

		} else {
			knownAgentsGraphVertexColor.setSelectedVertex(vertex);
			knownAgentsGraphVertexColor.setVerticesWithChangedColor(model.getFinalKnownAgents(vertex));
			knownAgentsGraphEdgeColor.setEdgesWithChangedColor(new HashSet<String>(model.getKnownAgentsGraph()
					.getOutEdges(vertex)));
		}
	}

	@Override
	public void updateSimulationFinished() {
		controller.simulationFinished();
	}

	@Override
	public void updateStepInfo() {
		Step step = model.getCurrentStep();
		if (step != null) {
			txtrInfoMsg.setText((step.getAgent() == null ? "" : step.getAgent() + ": ") + step.getInfo() + nl + nl);
			if (step.receivedMessage()) {
				txtrInfoMsg.append("from: " + step.getReceivedMessage().getSender() + nl + nl);
				txtrInfoMsg.append("content: " + step.getReceivedMessage().getContent());
			} else if (step.sentMessage()) {
				txtrInfoMsg.append("to: " + step.getAddedMessages().iterator().next().getReceivers() + nl + nl);
				txtrInfoMsg.append("content: " + step.getAddedMessages().iterator().next().getContent());
			}
		}
	}

	@Override
	public void updateStepNr() {
		int stepNr = model.getCurrentStepNr();
		lblStep.setText(String.valueOf(stepNr));
	}
}
