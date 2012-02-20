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
import com.github.api.v2.services.GitHubException;
import com.github.api.v2.services.UserService;

public class UserMiner extends BaseMiner {
    private UserService service = null;
    private Logger log;

    public UserMiner(UserService service) {
        super();
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
        try {
            log.trace("Fetching user: {}", username);
            User user = service.getUserByUsername(username);
            log.debug("Fetched user: {} email: {}", username, user==null?"null":user.getEmail());
            return user;
        } catch (Exception e) {
            log.error("Received exception attempting to fetch information for user: {}", username);
            return null;
        }
    }

    /**
     * Gets the followers of the user
     * 
     * @param username the username to fetch
     * @return a List of the users
     */
    public List<String> getUserFollowers(String username) {
        if (username == null || username.trim().equals("")) {
            log.warn("empty string passed into getUserFollowers");
            return null;
        }
        try {
            log.trace("Fetching user followers: {}", username);
            List<String> rv = service.getUserFollowers(username);
            log.debug("Fetched users followers: {} number: {}", username, rv==null?"null":rv.size());
            return rv;
        } catch (NullPointerException e) {
            log.error("Error fetching followers for user: {}", username, e);
            return null;
        }
    }

    public List<String> getUserFollowing(String username) {
        log.trace("Fetching users following: {}", username);
        List<String> rv = service.getUserFollowing(username);
        log.debug("Fetched users following: {} number: {}", username, rv==null?"null":rv.size());
        return rv;
    }

    public List<Repository> getWatchedRepositories (String username) {
        log.trace("Fetching watched repositories: {}", username);
        try {
            List <Repository> rv = service.getWatchedRepositories(username);
            log.debug("Fetched watched repositories: {} number: {}", username, rv==null?"null":rv.size());
            return rv;
        } catch (NullPointerException e) {
            log.error("Null pointer fetching repositories for user: {}", username, e);
            return null;
        }
    }
}
