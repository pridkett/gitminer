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

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.WatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatcherMinerV3 extends AbstractMiner {
    private final WatcherService service;

    private static final Logger log = LoggerFactory.getLogger(WatcherMinerV3.class); // NOPMD

    public WatcherMinerV3(final IGitHubClient ghc) {
        super();
        service = new WatcherService(ghc);
    }

    public List<User> getWatchers(final IRepositoryIdProvider repo) {
        List<User> watchers = null;
        try {
            watchers = service.getWatchers(repo);
        } catch (IOException e) {
            log.error("IOException in getting watchers for repository: {}", repo.generateId(), e);
        }
        return watchers;
    }

    public List<Repository> getWatched(final String login) {
        List<Repository> repos = null;
        try {
            repos = service.getWatched(login);
        } catch (IOException e) {
            log.error("IOException in getWatched: {}", login, e);
        }
        return repos;
    }
}
