package net.wagstrom.research.github.v3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;

import net.wagstrom.research.github.ApiThrottle;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottledGitHubInvocationHandler implements InvocationHandler {
    IGitHubClient wrapped;
    ApiThrottle throttle;
    private Logger log;
    private static final long SLEEP_DELAY = 5000;
    private static final long MAX_SLEEP_DELAY = SLEEP_DELAY * 5;

    private long failSleepDelay = SLEEP_DELAY;
    // this acts as a shared white list of methods that don't get throttled
    private static final HashSet<String> methods = new HashSet<String>(Arrays.asList("getRateLimit", "getRateLimitRemaining", "getRequestHeaders"));


    public ThrottledGitHubInvocationHandler(IGitHubClient s, ApiThrottle t) {
        wrapped = s;
        throttle = t;
        log = LoggerFactory.getLogger(ThrottledGitHubInvocationHandler.class);
    }

    private void failSleep() {
        try {
            Thread.sleep(failSleepDelay);
            failSleepDelay = failSleepDelay + SLEEP_DELAY;
        } catch (InterruptedException e) {
            log.error("Sleep interrupted",e);
        }
    }

    private Object handleInvocationException(RequestException e, Object proxy, Method method, Object[] args) throws Throwable {
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
            log.error("Connection exception: Method: {}, Args: {}", method.getName(), args);
            failSleep();
            return invoke(proxy, method, args);
        } else if (e.getCause() != null && e.getCause().getCause() instanceof ConnectException) {
            log.error("Connection exception (deep): Method: {}, Args: {}", method.getName(), args);
            failSleep();
            return invoke(proxy, method, args);			
        }

        log.error("Unhandled exception: Method: {} Args: {}", method.getName(), args);
        throw e.getCause();
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        log.trace("Method invoked: {}", method.getName());
        if (methods.contains(method.getName()))
            return method.invoke(wrapped, args);
        throttle.callWait();
        try {
            Object rv = method.invoke(wrapped, args);
            // log.warn("Rate limit: {}/{}", wrapped.getRateLimitRemaining(), wrapped.getRateLimit());
            throttle.setRateLimit(wrapped.getRateLimit());
            throttle.setRateLimitRemaining(wrapped.getRateLimitRemaining());
            failSleepDelay = SLEEP_DELAY;
            return rv;
        } catch (UndeclaredThrowableException e) {
            log.error("Undeclared Throwable Exception (propagated):", e);
            throw e.getCause();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RequestException) {
                return handleInvocationException((RequestException) e.getCause(), proxy, method, args);
            } else {
                log.error("Invocation target exception (propagated):", e);
                throw e.getCause();
            }
        }
    }

    public static IGitHubClient createThrottledGitHubClient(IGitHubClient toWrap, ApiThrottle throttle) {
        return (IGitHubClient)(Proxy.newProxyInstance(IGitHubClient.class.getClassLoader(),
                new Class[] {IGitHubClient.class},
                new ThrottledGitHubInvocationHandler(toWrap, throttle)));		
    }
}
