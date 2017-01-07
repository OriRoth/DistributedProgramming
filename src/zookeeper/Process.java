package zookeeper;

public class Process {
	int processId;
	boolean decide;
	byte[] vote;
	byte[] v; // value
	int n; // #processes
	int crashRound;

	public int getCrashRound() {
		return crashRound;
	}

	public void setCrashRound(int crashRound) {
		this.crashRound = crashRound;
	}

	public char getCrashLocation() {
		return crashLocation;
	}

	public void setCrashLocation(char crashLocation) {
		this.crashLocation = crashLocation;
	}

	char crashLocation;

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public Process(int processId, int n, byte[] vote, int crashRound, char crashLocation) {
		this.processId = processId;
		this.vote = vote;
		this.n = n;
		this.crashRound = crashRound;
		this.crashLocation = crashLocation;

	}

	public boolean getDecide() {
		return decide;
	}

	public void setDecide(boolean decide) {
		this.decide = decide;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public byte[] getVote() {
		return vote;
	}

	public void setVote(byte[] vote) {
		this.vote = vote;
	}

	public void print() {
		System.out.println(n + " " + processId + " " + vote[0] + " " + crashRound + " " + crashLocation);
	}

}
