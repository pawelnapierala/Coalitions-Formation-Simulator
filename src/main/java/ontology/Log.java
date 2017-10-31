package ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Log implements IFact, Comparable<Log> {

	private static final long serialVersionUID = -828960434108617324L;

	public static final String NEW_COALITION = "new coalition";

	private LogInfo info = null;
	private String author = null;
	private Long time = null;
	private Object content = null;

	public Log(LogInfo info, String author, Object content) {
		super();
		this.info = info;
		this.author = author;
		this.time = System.currentTimeMillis();
		this.content = content;
	}

	public Log(LogInfo info, String author, Object content, Long time) {
		super();
		this.info = info;
		this.author = author;
		this.time = time;
		this.content = content;
	}

	@Override
	public int compareTo(Log log) {
		return ((Long) this.getTime()).compareTo(log.getTime());
	}

	public String getAuthor() {
		return author;
	}

	public Object getContent() {
		return content;
	}

	public LogInfo getInfo() {
		return info;
	}

	public long getTime() {
		return time;
	}

	public boolean receivedMessage() {
		return content instanceof Message && !author.equals(((Message) content).getSender());
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public void setInfo(LogInfo info) {
		this.info = info;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
