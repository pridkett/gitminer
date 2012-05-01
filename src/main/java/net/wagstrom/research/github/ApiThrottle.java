/*
 * Copyright (c) 2011-2012 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wagstrom.research.github;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
    private static final Logger log = LoggerFactory.getLogger(ApiThrottle.class); // NOPMD
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private long internalMaxRate = -1;
    private String idstr = "";

    public ApiThrottle() {
        limit = -1;
        limitRemaining = -1;
    }


    /**
     * 
     * FIXME: this method is really tied to GitHub's limits right now.
     * Need to implement a mechanism to better understand when limits expire
     * based on time or count.
     * 
     */
    public void callWait() throws InterruptedException {
        if (lastReset != null) {
            log.debug("[{}] API Estimates: Limit: {} Remaining: {} Last Reset: {}", new Object[] {idstr, limit, limitRemaining, dateFormatter.format(lastReset.getTime())});
        }
        if (limitRemaining < 1 && limit != -1) {
            Calendar sleepEnd = (Calendar)lastReset.clone();
            int timeDiff = 3600;
            if (limit == 60) {
                timeDiff = 65;
            } else if (limit == 5000) {
                timeDiff = 3610;
            }
            sleepEnd.add(Calendar.SECOND, timeDiff);
            long sleepTime = sleepEnd.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
            if (sleepTime > 0) {
                log.info("[{}] Sleeping for {}ms", idstr, sleepTime);
                Thread.sleep(sleepTime);
            } else {
                log.info("[{}] Should be no reason to sleep", idstr);
            }
        }
        if (internalMaxRate != -1 && lastCall != null) {
            long sleepTime = internalMaxRate - (Calendar.getInstance().getTimeInMillis() - lastCall.getTimeInMillis());
            if (sleepTime > 0) {
                log.trace("[{}] Exceeded internal rate. Sleeping for {}ms", idstr, sleepTime);
                Thread.sleep(sleepTime);
            }
        }
        lastCall = Calendar.getInstance();
    }

    public void setRateLimit(final int limit) {
        this.limit = limit;
    }

    public void setRateLimitRemaining(final int limitRemaining) {
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
    public void setMaxRate(final int calls, final int seconds) {
        internalMaxRate = (long)(((double) seconds / (double)calls)*1000);
        log.trace("[{}] Internal maximum rate set to: {}ms", idstr, internalMaxRate);
    }


    public String getId() {
        return idstr;
    }


    public void setId(String idstr) {
        this.idstr = idstr;
    }
}
