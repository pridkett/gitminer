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

import com.github.api.v2.services.GitHubServiceFactory;

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
		
		BlueprintsDriver bp = connectToGraph(p);

		RepositoryMiner rm = new RepositoryMiner(ThrottledGitHubInvocationHandler.createThrottledRepositoryService(factory.createRepositoryService(), throttle));
		for (String proj : projects) {
			String [] projsplit = proj.split("/");
			bp.saveRepository(rm.getRepositoryInformation(projsplit[0], projsplit[1]));
		}
	
		UserMiner um = new UserMiner(ThrottledGitHubInvocationHandler.createThrottledUserService(factory.createUserService(), throttle));
		for (String user : users) {
			bp.saveUser(um.getUserInformation(user));
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
