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
import java.util.Collection;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationMinerV3 extends AbstractMiner {
    private final OrganizationService service;

    private static final Logger log = LoggerFactory.getLogger(OrganizationMinerV3.class); // NOPMD

    public OrganizationMinerV3(final IGitHubClient ghc) {
        super();
        service = new OrganizationService(ghc);
    }

    public Collection<User> getMembers(final String organization) {
        Collection<User> members = null;
        try {
            members = service.getMembers(organization);
        } catch (IOException e) {
            log.error("IOException getting organization members for {}: {}", organization, e);
        }
        return members;
    }

    public Collection<User> getPublicMembers(final String organization) {
        Collection<User> members = null;
        try {
            members = service.getPublicMembers(organization);
        } catch (IOException e) {
            log.error("IOException getting public organization members for {}: {}", organization, e);
        }
        return members;
    }

    public User getOrganization(final String organization) {
        User org = null;
        try {
            org = service.getOrganization(organization);
        } catch (IOException e) {
            log.error("IOException getting organization {}: {}", organization, e);
        }
        return org;
    }
}
