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
	private Logger log;
	
	public ThrottledGitHubInvocationHandler(GitHubService s) {
		wrapped = s;
		log = LoggerFactory.getLogger(ThrottledGitHubInvocationHandler.class);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		log.info("Method invoked: " + method.getName());
		return null;
		// return method.invoke(wrapped, args);
	}

	public static UserService createThrottledUserService(UserService toWrap) {
        return (UserService)(Proxy.newProxyInstance(UserService.class.getClassLoader(),
                new Class[] {UserService.class},
                    new ThrottledGitHubInvocationHandler(toWrap)));
	}
}
