package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMinerV3 extends AbstractMiner {
    private final UserService service;

    private static final Logger log = LoggerFactory.getLogger(UserMinerV3.class); // NOPMD

    public UserMinerV3(final IGitHubClient ghc) {
        super();
        service = new UserService(ghc);
    }

    public User getUser(final String login) {
        User user = null;
        try {
            user = service.getUser(login);
        } catch (IOException e) {
            log.error("IOException in getting user {} {}", login, e);
        }
        return user;
    }

    public List<User> getFollowers(final String login) {
        List<User> followers = null;
        try {
            followers = service.getFollowers(login);
        } catch (IOException e) {
            log.error("IOException in getFollowers: {}", login, e);
        }
        return followers;
    }

    public List<User> getFollowing(final String login) {
        List<User> following = null;
        try {
            following = service.getFollowing(login);
        } catch (IOException e) {
            log.error("IOException in getFollowing: {}", login, e);
        }
        return following;
    }
}
