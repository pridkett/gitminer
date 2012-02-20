package net.wagstrom.research.github;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Gist;
import com.github.api.v2.services.GistService;
import com.github.api.v2.services.GitHubException;

public class GistMiner {
    private GistService service = null;
    private Logger log;

    public GistMiner(GistService service) {
        this.service = service;
        log = LoggerFactory.getLogger(this.getClass());
    }

    public List<Gist> getUserGists(String user) {
        try {
            log.trace("Fetching gists for user: {}", user);
            List<Gist> gists = service.getUserGists(user);
            log.debug("Fetched gists for user: {} number: {}", user, gists==null?"null":gists.size());
            return gists;
        } catch (GitHubException e) {
            log.error("Received GitHub Exception attempting to fetch gists for user: {}", user);
            return null;
        } catch (NullPointerException e) {
            log.error("Received null pointer exception attempting to fetch gists for user: {}", user, e);
            return null;
        }
    }

}
