package net.wagstrom.research.github.v3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashSet;

import net.wagstrom.research.github.ApiThrottle;
import net.wagstrom.research.github.InvocationHandlerBase;

import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottledGitHubInvocationHandler extends InvocationHandlerBase implements InvocationHandler {
    IGitHubClient wrapped;
    ApiThrottle throttle;
    private Logger log;
 
    // this acts as a shared white list of methods that don't get throttled
    private static final HashSet<String> methods = new HashSet<String>(Arrays.asList("getRateLimit", "getRateLimitRemaining", "getRequestHeaders"));


    public ThrottledGitHubInvocationHandler(IGitHubClient s, ApiThrottle t) {
        super();
        wrapped = s;
        throttle = t;
        log = LoggerFactory.getLogger(ThrottledGitHubInvocationHandler.class);
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
