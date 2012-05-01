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

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GistMinerV3 extends AbstractMiner {
    private final GistService service;

    private static final Logger log = LoggerFactory.getLogger(GistMinerV3.class); // NOPMD

    public GistMinerV3(final IGitHubClient ghc) {
        super();
        service = new GistService(ghc);
    }

    public List<Gist> getGists(final String user) {
        List<Gist> gists = null;
        try {
            gists = service.getGists(user);
        } catch (IOException e) {
            log.error("IOException in getGists: {}", user, e);
        } catch (NullPointerException npe) {
            log.error("NullPointerException getting gists: {}", user, npe);
        }
        return gists;
    }

}
