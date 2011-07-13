package net.wagstrom.research.github;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.services.GitHubService;
import com.github.api.v2.services.UserService;

public class ThrottledGitHubInvocationHandler implements InvocationHandler {
	GitHubService wrapped;
	ApiThrottle throttle;
	private Logger log;
	
	public ThrottledGitHubInvocationHandler(GitHubService s, ApiThrottle t) {
		wrapped = s;
		throttle = t;
		log = LoggerFactory.getLogger(ThrottledGitHubInvocationHandler.class);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		log.info("Method invoked: " + method.getName());
		throttle.callWait();
		return method.invoke(wrapped, args);
	}

	public static UserService createThrottledUserService(UserService toWrap, ApiThrottle throttle) {
        return (UserService)(Proxy.newProxyInstance(UserService.class.getClassLoader(),
                new Class[] {UserService.class},
                    new ThrottledGitHubInvocationHandler(toWrap, throttle)));
	}
}
