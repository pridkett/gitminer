/*
 * Copyright (c) 2011-2012 IBM Corporation
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
        } catch (NullPointerException npe) {
            log.error("NullPointerException in getFollowers: {}", login, npe);
        }
        return followers;
    }

    public List<User> getFollowing(final String login) {
        List<User> following = null;
        try {
            following = service.getFollowing(login);
        } catch (IOException e) {
            log.error("IOException in getFollowing: {}", login, e);
        } catch (NullPointerException npe) {
            log.error("NullPointerException in getFollowing: {}", login, npe);
        }
        return following;
    }
}
