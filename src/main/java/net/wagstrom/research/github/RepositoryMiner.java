package net.wagstrom.research.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Repository;
import com.github.api.v2.services.RepositoryService;
import com.github.api.v2.services.UserService;

public class RepositoryMiner {
	private RepositoryService service = null;
	private Logger log;
	
	public RepositoryMiner(RepositoryService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public Repository getRepositoryInformation(String username, String reponame) {
		Repository repo = service.getRepository(username, reponame);
		log.info(repo.getName());
		log.info(repo.getParent());
		log.info(repo.getSource());
		return repo;
	}

}
