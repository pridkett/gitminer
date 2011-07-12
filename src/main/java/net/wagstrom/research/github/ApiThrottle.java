package net.wagstrom.research.github;

public class ApiThrottle {
	static int calls;
	
	public ApiThrottle() {
		calls = 0;
	}
	
	public void callWait() {
		calls = calls + 1;
	}
	
	public int getCalls() {
		return calls;
	}
}
