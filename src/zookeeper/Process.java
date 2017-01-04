package zookeeper;

public class Process {
	int processId;
	boolean isActive;
	boolean decide;
	int vote;
//	int r; //round
//	int c; //coordinator
//	int est; //estimation
	int v; //value
	int n; //#processes
//	int est_from_c;
	int crashRound;
	char crashLocation;
	
	public int getN() {
		return n;
	}


	public void setN(int n) {
		this.n = n;
	}


	public Process(int processId,  int n, int vote, int crashRound, char crashLocation) {
		this.processId = processId;
		this.isActive = true;
		this.vote = vote;
		this.n = n;
		this.crashRound=crashRound;
		this.crashLocation=crashLocation;
		
	}
	
	
	public boolean getDecide() {
		return decide;
	}

	public void setDecide(boolean decide) {
		this.decide = decide;
	}

//	public int getEst_from_c() {
//		return est_from_c;
//	}
//
//	public void setEst_from_c(int est_from_c) {
//		this.est_from_c = est_from_c;
//	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}


	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int getVote() {
		return vote;
	}

	public void setVote(int vote) {
		this.vote = vote;
	}

	public void print(){
		System.out.println(processId+" "+n+" "+vote+" "+crashRound+" "+crashLocation);
	}

}

