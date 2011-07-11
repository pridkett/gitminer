package net.wagstrom.research.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.User;
import com.github.api.v2.services.UserService;

public class UserMiner {
	private UserService service = null;
	private Logger log;
	
	public UserMiner(UserService service) {
		this.service = service;
		log = LoggerFactory.getLogger(UserMiner.class);
	}
	
	public User getUserInformation(String username) {
		User user = service.getUserByUsername(username);
		log.info(user.getEmail());
		return user;
	}
}
