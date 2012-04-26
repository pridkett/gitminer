package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMinerV3 extends AbstractMiner {
    private UserService service;
    
    private static final Logger log = LoggerFactory.getLogger(UserMinerV3.class); // NOPMD

    public UserMinerV3(IGitHubClient ghc) {
        service = new UserService(ghc);
    }

    
    public User getUser(String login) {
        try {
            return service.getUser(login);
        } catch (IOException e) {
            log.error("IOException in getting user {} {}", login, e);
            return null;
        }
    }
    
    public List<User> getFollowers(String login) {
        try {
            return service.getFollowers(login);
        } catch (IOException e) {
            log.error("IOException in getFollowers: {}", login, e);
            return null;
        }
    }
    
    public List<User> getFollowing(String login) {
        try {
            return service.getFollowing(login);
        } catch (IOException e) {
            log.error("IOException in getFollowing: {}", login, e);
            return null;
        }
    }
}
