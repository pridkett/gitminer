package net.wagstrom.research.github;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.services.GitHubService;
import com.github.api.v2.services.RepositoryService;
import com.github.api.v2.services.UserService;

public class ThrottledGitHubInvocationHandler implements InvocationHandler {
	GitHubService wrapped;
	ApiThrottle throttle;
	private Logger log;
	// this acts as a shared white list of methods that don't get throttled
	private static final HashSet<String> methods = new HashSet<String>(Arrays.asList("getRateLimit", "getRateLimitRemaining", "getRequestHeaders"));
	
	
	public ThrottledGitHubInvocationHandler(GitHubService s, ApiThrottle t) {
		wrapped = s;
		throttle = t;
		log = LoggerFactory.getLogger(ThrottledGitHubInvocationHandler.class);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		log.info("Method invoked: " + method.getName());
		if (methods.contains(method.getName()))
			return method.invoke(wrapped, args);
		throttle.callWait();
		Object rv = method.invoke(wrapped, args);
		throttle.setRateLimit(wrapped.getRateLimit());
		throttle.setRateLimitRemaining(wrapped.getRateLimitRemaining());
		return rv;
	}

	public static UserService createThrottledUserService(UserService toWrap, ApiThrottle throttle) {
        return (UserService)(Proxy.newProxyInstance(UserService.class.getClassLoader(),
                new Class[] {UserService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));
	}
	
	public static RepositoryService createThrottledRepositoryService(RepositoryService toWrap, ApiThrottle throttle) {
        return (RepositoryService)(Proxy.newProxyInstance(RepositoryService.class.getClassLoader(),
                new Class[] {RepositoryService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));		
	}
}
