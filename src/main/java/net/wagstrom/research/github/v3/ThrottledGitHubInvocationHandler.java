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
package net.wagstrom.research.github.v3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.wagstrom.research.github.ApiThrottle;
import net.wagstrom.research.github.AbstractInvocationHandler;

import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottledGitHubInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {
    private final IGitHubClient wrapped;
    private final ApiThrottle throttle;
    private static final Logger log = LoggerFactory.getLogger(ThrottledGitHubInvocationHandler.class); // NOPMD
 
    // this acts as a shared white list of methods that don't get throttled
    private static final Set<String> METHODS = new HashSet<String>(Arrays.asList("getRateLimit", "getRateLimitRemaining", "getRequestHeaders"));


    public ThrottledGitHubInvocationHandler(final IGitHubClient client, final ApiThrottle throttle) {
        super();
        wrapped = client;
        this.throttle = throttle;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        Object returnVal = null;
        log.trace("Method invoked: {}", method.getName());
        if (METHODS.contains(method.getName())) {
            returnVal = method.invoke(wrapped, args);
        } else {
        
            throttle.callWait();
            
            try {
                returnVal = method.invoke(wrapped, args);
                // log.warn("Rate limit: {}/{}", wrapped.getRateLimitRemaining(), wrapped.getRateLimit());
                throttle.setRateLimit(wrapped.getRateLimit());
                throttle.setRateLimitRemaining(wrapped.getRateLimitRemaining());
                failSleepDelay = SLEEP_DELAY;
            } catch (UndeclaredThrowableException e) {
                log.error("Undeclared Throwable Exception (propagated):", e);
                throw e.getCause();
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RequestException) {
                    returnVal = handleInvocationException((RequestException) e.getCause(), proxy, method, args);
                } else {
                    log.error("Invocation target exception (propagated):", e);
                    throw e.getCause();
                }
            }
        }
        return returnVal;
    }

    public static IGitHubClient createThrottledGitHubClient(final IGitHubClient toWrap, final ApiThrottle throttle) {
        return (IGitHubClient)(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[] {IGitHubClient.class},
                new ThrottledGitHubInvocationHandler(toWrap, throttle)));		
    }
}
