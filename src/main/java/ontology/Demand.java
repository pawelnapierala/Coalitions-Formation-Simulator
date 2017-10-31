package ontology;

import java.util.UUID;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Demand implements IFact {

	private static final long serialVersionUID = -4565525492932117182L;

	private UUID invitationId = null;
	private double demand;

	public Demand(UUID invitationId, double demand) {
		this.invitationId = invitationId;
		this.demand = demand;
	}

	public double getDemand() {
		return demand;
	}

	public UUID getInvitationId() {
		return invitationId;
	}
}