package net.wagstrom.research.github;

/**
 * ApiThrottle is a simple class that is used by ThrottledGitHubInvovcationHandler to
 * throttle back connections. Eventually I'd like to make it so you have multiple models
 * of throttling, but this is what we get for now.
 * 
 * As of right now this basically has to know the throttling system itself, it doesn't get
 * any feedback from the API it's throttling about when limits are up or anything like that.
 * That would be a more advanced version of this class.
 * 
 * @author patrick
 *
 */
public class ApiThrottle {
	static int calls;
	
	public ApiThrottle() {
		calls = 0;
	}
	
	public void callWait() {
		calls = calls + 1;
		return;
	}
	
	public int getCalls() {
		return calls;
	}
}
