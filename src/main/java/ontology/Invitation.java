package ontology;

import java.util.Set;
import java.util.UUID;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Invitation implements IFact {

	private static final long serialVersionUID = 6683485393622555565L;

	private UUID id = null;
	private String inviter;
	private Set<String> agents = null;
	private long timeStamp;

	public Invitation(String inviter, Set<String> agents) {
		this.id = UUID.randomUUID();
		this.inviter = inviter;
		this.agents = agents;
		this.timeStamp = System.currentTimeMillis();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Invitation))
			return false;
		Invitation other = (Invitation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Set<String> getAgents() {
		return agents;
	}

	public UUID getId() {
		return id;
	}

	public String getInviter() {
		return inviter;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public void setInviter(String inviter) {
		this.inviter = inviter;
	}
}