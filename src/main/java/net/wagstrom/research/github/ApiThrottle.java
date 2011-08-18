package net.wagstrom.research.github;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private int limit;
	private int limitRemaining;
	private Calendar lastReset = null;
	private Calendar lastCall = null;
	private Logger log;
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	private SimpleDateFormat dateFormatter = null;
	private long internalMaxRate = -1;
	
	public ApiThrottle() {
		limit = -1;
		limitRemaining = -1;
		log = LoggerFactory.getLogger(this.getClass());
		dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	}
	
	
	/**
	 * 
	 * FIXME: this method is really tied to GitHub's limits right now.
	 * Need to implement a mechanism to better understand when limits expire
	 * based on time or count.
	 * 
	 */
	public void callWait() throws InterruptedException {
		if (lastReset != null)
			log.debug("API Estimates: Limit: {} Remaining: {} Last Reset: {}", new Object[] {limit, limitRemaining, dateFormatter.format(lastReset.getTime())});
		if (limitRemaining < 1 && limit != -1) {
			Calendar sleepEnd = (Calendar)lastReset.clone();
			int timeDiff = 3600;
			if (limit == 60) {
				timeDiff = 65;
			} else if (limit == 5000) {
				timeDiff = 86460;
			}
			sleepEnd.add(Calendar.SECOND, timeDiff);
			long sleepTime = sleepEnd.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			if (sleepTime > 0) {
				log.info("Sleeping for {}ms", sleepTime);
				Thread.sleep(sleepTime);
			} else {
				log.info("Should be no reason to sleep");
			}
		}
		if (internalMaxRate != -1 && lastCall != null) {
			long sleepTime = internalMaxRate - (Calendar.getInstance().getTimeInMillis() - lastCall.getTimeInMillis());
			if (sleepTime > 0) {
				log.trace("Exceeded internal rate. Sleeping for {}ms", sleepTime);
				Thread.sleep(sleepTime);
			}
		}
		lastCall = Calendar.getInstance();
		return;
	}
	
	public void setRateLimit(int limit) {
		this.limit = limit;
	}
	
	public void setRateLimitRemaining(int limitRemaining) {
		this.limitRemaining = limitRemaining;
		// assume that we just reset the time limit
		if (this.limitRemaining == this.limit - 1) {
			lastReset = Calendar.getInstance();
		}
	}
	
	/**
	 * Sets the maximum rate as the number of calls in the number of seconds
	 * 
	 * This is external from the given API rate and is used to be nice to servers.
	 * Internally it is stored as a long indicating the minimum wait between calls
	 * 
	 * @param calls
	 * @param seconds
	 */
	public void setMaxRate(int calls, int seconds) {
		internalMaxRate = (long)(((double) seconds / (double)calls)*1000);
		log.trace("Internal maximum rate set to: {}ms", internalMaxRate);
	}
}
