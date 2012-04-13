package net.wagstrom.research.github;

import java.lang.reflect.Method;
import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractInvocationHandler.class); // NOPMD
    protected static final long SLEEP_DELAY = 5000;
    protected static final long MAX_SLEEP_DELAY = SLEEP_DELAY * 5;

    protected long failSleepDelay = SLEEP_DELAY;
    
    public AbstractInvocationHandler() {
    }
    
    private void failSleep() {
        try {
            Thread.sleep(failSleepDelay);
            failSleepDelay = failSleepDelay + SLEEP_DELAY;
        } catch (InterruptedException e) {
            log.error("Sleep interrupted",e);
        }
    }
    
    public abstract Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable;

    protected Object handleInvocationException(Exception e, Object proxy, Method method, Object[] args) throws Throwable {
        if (failSleepDelay > MAX_SLEEP_DELAY) {
            log.error("Too many failures. Giving up and returning null");
            log.error("method: {} args: {}", method, args);
            return null;
        }

        if (e.getMessage().startsWith("API Rate Limit Exceeded for")) {
            log.warn("Exceeding API rate limit -- Sleep for {}ms and try again", failSleepDelay);
            failSleep();
            return invoke(proxy, method, args);
        } else if (e.getMessage().toLowerCase().indexOf("<title>server error - github</title>") != -1) {
            log.warn("Received a server error from GitHub -- Sleep for {}ms and try again", failSleepDelay);
            failSleep();
            return invoke(proxy, method, args);
        } else if (e.getMessage().trim().toLowerCase().equals("{\"error\":\"not found\"}")) {
            log.warn("GitHub returned Not Found: Method: {}, Args: {}", method.getName(), args);
            return null;
        } else if (e.getCause() instanceof ConnectException) {
            log.error("Connection exception: Method: {}, Args: {}", new Object[]{method.getName(), args, e});
            failSleep();
            return invoke(proxy, method, args);
        } else if (e.getCause() != null && e.getCause().getCause() instanceof ConnectException) {
            log.error("Connection exception (deep): Method: {}, Args: {}", new Object[]{method.getName(), args, e});
            failSleep();
            return invoke(proxy, method, args);
        }

        log.error("Unhandled exception: Method: {} Args: {}", new Object[]{method.getName(), args, e});
        failSleep();
        return invoke(proxy, method, args); 
    }
}
