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
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

/**
 * Main driver class for GitHub data processing.
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class GitHubMain {
	Logger log = null;
	
	public GitHubMain() {
		log = LoggerFactory.getLogger(this.getClass());		
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
		
		IndexableGraph graph = connectToGraph(p);
		RepositoryMiner rm = new RepositoryMiner(factory.createRepositoryService());
		for (String proj : projects) {
			String [] projsplit = proj.split("/");
			rm.getRepositoryInformation(projsplit[0], projsplit[1], graph);
		}
	
		UserMiner um = new UserMiner(factory.createUserService());
		for (String user : users) {
			um.getUserInformation(user, graph);
		}
		
		log.info("Shutting down graph");
		graph.shutdown();
	}
	
	private IndexableGraph connectToGraph(Properties p) {
		IndexableGraph graph = null;
		
		try {
			String dbapi = p.getProperty("net.wagstrom.research.github.dbengine").trim();
			String dburl = p.getProperty("net.wagstrom.research.github.dburl").trim();
			if (dbapi.toLowerCase().equals("neo4j")) {
				log.info("opening neo4j graph at " + dburl);
				graph = new Neo4jGraph(dburl);
			}
		} catch (NullPointerException e) {
			log.error("properties undefined, must define both net.wagstrom.research.github.dbengine and net.wagstrom.research.github.dburl");
		}
		return graph;
	}
}
