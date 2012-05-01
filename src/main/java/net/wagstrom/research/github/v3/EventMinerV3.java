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

import java.util.List;

import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventMinerV3 extends AbstractMiner {
    private final EventServiceV3 service;

    private static final Logger log = LoggerFactory.getLogger(EventMinerV3.class); // NOPMD

    public EventMinerV3(final IGitHubClient ghc) {
        super();
        service = new EventServiceV3(ghc);
    }

    public List<Event> getUserEvents(final String user) {
        log.trace("Getting all events for user {}", user);
        List<Event> events = null;
        try {
            events = service.getUserEvents(user);
        } catch (NullPointerException npe) {
            log.error("NullPointerException getting events for user: {}", user, npe);
        }
        return events;
    }

}
