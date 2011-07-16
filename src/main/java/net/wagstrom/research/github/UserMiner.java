/*
 * Copyright 2011 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wagstrom.research.github;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Repository;
import com.github.api.v2.schema.User;
import com.github.api.v2.services.UserService;

public class UserMiner {
	private UserService service = null;
	private Logger log;
	
	public UserMiner(UserService service) {
		this.service = service;
		log = LoggerFactory.getLogger(UserMiner.class);
	}
	
	/**
	 * Gets a user information and saves it into the graph.
	 * 
	 * This really should be separated to not directly save the data into the graph here, but I'm lazy for right now.
	 * 
	 * @param username
	 * @param graph
	 * @return
	 */
	public User getUserInformation(String username) {
		User user = service.getUserByUsername(username);
		log.debug("Fetched user: " + username + " email: " + user.getEmail());
		return user;
	}
	
	/**
	 * Gets the followers of the user
	 * 
	 * @param username the username to fetch
	 * @return a List of the users
	 */
	public List<String> getUserFollowers(String username) {
		List<String> rv = service.getUserFollowers(username);
		log.debug("Fetched users followers: " + username + " number: " + rv.size());
		return rv;
	}
	
	public List<String> getUserFollowing(String username) {
		List<String> rv = service.getUserFollowing(username);
		log.debug("Fetched users following: " + username + " number: " + rv.size());
		return rv;
	}
	
	public List<Repository> getWatchedRepositories (String username) {
		List <Repository> rv = service.getWatchedRepositories(username);
		log.debug("Fetched watched repositories: " + username + " number: " + rv.size());
		return rv;
	}
}
