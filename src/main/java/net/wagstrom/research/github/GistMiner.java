package net.wagstrom.research.github;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Gist;
import com.github.api.v2.services.GistService;

public class GistMiner {
	private GistService service = null;
	private Logger log;
	
	public GistMiner(GistService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public List<Gist> getUserGists(String user) {
		List<Gist> gists = service.getUserGists(user);
		log.debug("Fetched gists for user: {} number: {}", user, gists.size());
		return gists;
	}
	
}
