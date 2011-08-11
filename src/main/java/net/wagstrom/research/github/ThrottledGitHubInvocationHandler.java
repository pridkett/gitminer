package net.wagstrom.research.github;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.services.GistService;
import com.github.api.v2.services.GitHubException;
import com.github.api.v2.services.GitHubService;
import com.github.api.v2.services.IssueService;
import com.github.api.v2.services.OrganizationService;
import com.github.api.v2.services.PullRequestService;
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
		log.trace("Method invoked: {}", method.getName());
		if (methods.contains(method.getName()))
			return method.invoke(wrapped, args);
		throttle.callWait();
		try {
			Object rv = method.invoke(wrapped, args);
			throttle.setRateLimit(wrapped.getRateLimit());
			throttle.setRateLimitRemaining(wrapped.getRateLimitRemaining());
			return rv;
		} catch (UndeclaredThrowableException e) {
			log.error("Undeclared Throwable Exception (propagated):", e);
			throw e.getCause();
		} catch (InvocationTargetException e) {
			log.error("Invocation target exception (propagated):", e);
			throw e.getCause();
		} catch (GitHubException e) {
			if (e.getMessage().startsWith("API Rate Limit Exceeded for")) {
				log.warn("Exceeding API rate limit. Throttle down and try again!");
				return invoke(proxy, method, args);
			} else {
				log.error("Unhandled GitHubException: ", e);
				throw e.getCause();
			}
		}
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

	public static OrganizationService createThrottledOrganizationService(OrganizationService toWrap, ApiThrottle throttle) {
        return (OrganizationService)(Proxy.newProxyInstance(OrganizationService.class.getClassLoader(),
                new Class[] {OrganizationService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));				
	}

	public static GistService createThrottledGistService(GistService toWrap, ApiThrottle throttle) {
        return (GistService)(Proxy.newProxyInstance(GistService.class.getClassLoader(),
                new Class[] {GistService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));						
	}
	
	public static IssueService createThrottledIssueService(IssueService toWrap, ApiThrottle throttle) {
        return (IssueService)(Proxy.newProxyInstance(IssueService.class.getClassLoader(),
                new Class[] {IssueService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));
	}

	public static PullRequestService createThrottledPullRequestService(PullRequestService toWrap, ApiThrottle throttle) {
        return (PullRequestService)(Proxy.newProxyInstance(PullRequestService.class.getClassLoader(),
                new Class[] {PullRequestService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));
	}
	
	/**
	 * This works at compile time and not runtime
	 * 
	 * @param toWrap
	 * @param throttle
	 * @return
	 */
	public static GitHubService createThrottledService(GitHubService toWrap, ApiThrottle throttle) {
		System.out.println(toWrap.getClass().toString());
        return (GitHubService)(Proxy.newProxyInstance(GitHubService.class.getClassLoader(),
                new Class[] {GitHubService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));				
	}	
}
