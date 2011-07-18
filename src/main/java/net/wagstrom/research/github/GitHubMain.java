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

import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.services.GitHubException;
import com.github.api.v2.services.GitHubServiceFactory;
import com.github.api.v2.services.OrganizationService;

/**
 * Main driver class for GitHub data processing.
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class GitHubMain {
	Logger log = null;
	ApiThrottle throttle = null;
	public GitHubMain() {
		log = LoggerFactory.getLogger(this.getClass());		
        throttle = new ApiThrottle();
	}
	
	public void main() {

		ArrayList <String> projects = new ArrayList<String> ();
		ArrayList <String> users = new ArrayList<String> ();
		ArrayList <String> organizations = new ArrayList<String> ();
		GitHubServiceFactory factory = GitHubServiceFactory.newInstance();
		
		Properties p = GithubProperties.props();
		
		try {
			for (String proj : p.getProperty("net.wagstrom.research.github.projects").split(",")) {
				projects.add(proj.trim());
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.projects undefined");
			System.exit(1);
		}
		
		try{
			for (String user : p.getProperty("net.wagstrom.research.github.users").split(",")) {
				users.add(user.trim());
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.users undefined");
			System.exit(1);
		}
		
		try {
			for (String organization : p.getProperty("net.wagstrom.research.github.organizations").split(",")){
				organizations.add(organization.trim());
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.organizations undefined");
			System.exit(1);
		}
		
		BlueprintsDriver bp = connectToGraph(p);

		RepositoryMiner rm = new RepositoryMiner(ThrottledGitHubInvocationHandler.createThrottledRepositoryService(factory.createRepositoryService(), throttle));
		for (String proj : projects) {
			String [] projsplit = proj.split("/");
			bp.saveRepository(rm.getRepositoryInformation(projsplit[0], projsplit[1]));
			bp.saveRepositoryCollaborators(proj, rm.getRepositoryCollaborators(projsplit[0], projsplit[1]));
			bp.saveRepositoryContributors(proj, rm.getRepositoryContributors(projsplit[0], projsplit[1]));
			bp.saveRepositoryWatchers(proj, rm.getWatchers(projsplit[0], projsplit[1]));
			bp.saveRepositoryForks(proj, rm.getForks(projsplit[0], projsplit[1]));
		}
	
		UserMiner um = new UserMiner(ThrottledGitHubInvocationHandler.createThrottledUserService(factory.createUserService(), throttle));
		for (String user : users) {
			bp.saveUser(um.getUserInformation(user));
			bp.saveUserFollowers(user, um.getUserFollowers(user));
			bp.saveUserFollowing(user, um.getUserFollowing(user));
			bp.saveUserWatchedRepositories(user, um.getWatchedRepositories(user));
			bp.saveUserRepositories(user, rm.getUserRepositories(user));
		}
	
		ThrottledGitHubInvocationHandler.createThrottledOrganizationService(factory.createOrganizationService(), throttle);
		
		OrganizationMiner om = new OrganizationMiner(ThrottledGitHubInvocationHandler.createThrottledOrganizationService(factory.createOrganizationService(), throttle));
		for (String organization : organizations) {
			bp.saveOrganization(om.getOrganizationInformation(organization));
//			try {
//				bp.saveOrganizationOwners(organization, om.getOrganizationOwners(organization));
//			} catch (Exception e) {
//				log.error("Exception trying to get owners for organization: {} exception: {}", organization, e);
//			}
			bp.saveOrganizationPublicMembers(organization, om.getOrganizationPublicMembers(organization));
			bp.saveOrganizationPublicRepositories(organization, om.getOrganizationPublicRepositories(organization));
		}
		
		log.info("Shutting down graph");
		bp.shutdown();
	}
	
	private BlueprintsDriver connectToGraph(Properties p) {
		BlueprintsDriver bp = null;
		
		try {
			String dbengine = p.getProperty("net.wagstrom.research.github.dbengine").trim();
			String dburl = p.getProperty("net.wagstrom.research.github.dburl").trim();
			bp = new BlueprintsDriver(dbengine, dburl);
		} catch (NullPointerException e) {
			log.error("properties undefined, must define both net.wagstrom.research.github.dbengine and net.wagstrom.research.github.dburl");
		}
		return bp;
	}
}
