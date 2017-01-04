package zookeeper;

public class Message {
	int round;
	int value;
	
	
	
	public int getRound() {
		return round;
	}



	public void setRound(int round) {
		this.round = round;
	}



	public int getValue() {
		return value;
	}



	public void setValue(int value) {
		this.value = value;
	}



	public Message(byte[] data, int version) {
		this.round = version; 
		this.value= Integer.valueOf(data.toString());
	}


}
