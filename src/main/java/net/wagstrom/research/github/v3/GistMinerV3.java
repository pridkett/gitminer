package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GistMinerV3 extends AbstractMiner {
    private GistService service;

    private static final Logger log = LoggerFactory.getLogger(GistMinerV3.class); // NOPMD

    public GistMinerV3(IGitHubClient ghc) {
        service = new GistService(ghc);
    }

    public List<Gist> getGists(String user) {
        try {
            return service.getGists(user);// TODO Auto-generated method stub
        } catch (IOException e) {
            log.error("IOException in getGists: {}", user, e);
            return null;
        }
    }

}
