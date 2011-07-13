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

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Repository;
import com.github.api.v2.schema.User;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

/**
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class BlueprintsDriver {
	private IndexableGraph graph = null;
	private Logger log = null;
	private SimpleDateFormat dateFormatter = null;
	
	/**
	 * Base constructor for BlueprintsDriver
	 * 
	 * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
	 * @param dburl The url of the database to use
	 */
	public BlueprintsDriver(String dbengine, String dburl) {
		log = LoggerFactory.getLogger(this.getClass());
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		
		if (dbengine.toLowerCase().equals("neo4j")) {
			log.info("opening neo4j graph at " + dburl);
			graph = new Neo4jGraph(dburl);
		} else {
			log.error("Unknown database engine: " + dbengine);
		}
	}
	
	/**
	 * Gets a reference to the specified index, creating it if it doesn't exist.
	 * 
	 * @param idxname the name of the index to load/create
	 * @return a reference to the loaded/created index
	 */
	public Index<? extends Element> getOrCreateIndex(String idxname) {
		Index<? extends Element> repoidx = null;
		for (Index<? extends Element> idx : graph.getIndices()) {
			log.debug("Found index name: " + idx.getIndexName() + " class: " + idx.getIndexClass().toString());
			if (idx.getIndexName().equals(idxname) && Vertex.class.isAssignableFrom(idx.getIndexClass())) {
				log.debug("Found matching index in database");
				repoidx =  idx;
				break;
			}
		}
		if (repoidx == null) {
			repoidx = graph.createManualIndex(idxname, Vertex.class);
		}
		return repoidx;
	}
	
	public void shutdown() {
		if (graph != null) {
			graph.shutdown();
		}
	}
	
	/**
	 * 
	 * FIXME: this should return the node
	 * 
	 * @param user
	 * @return
	 */
	public User saveUser(User user) {
		Index <Vertex> useridx = (Index <Vertex>)getOrCreateIndex("user-idx");
		Vertex node = null;
		for (Vertex v : useridx.get("login", user.getLogin())) {
			node = v;
		}
		if (node == null) {
			node = graph.addVertex(null);
			useridx.put("login", user.getLogin(), node);
		}

		log.info(user.toString());

		node.setProperty("type", "USER");
				
		if (user.getBlog() != null) node.setProperty("blog", user.getBlog());
		node.setProperty("collaborators", user.getCollaborators());
		if (user.getCompany() != null) node.setProperty("company", user.getCompany());
		if (user.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(user.getCreatedAt()));
		node.setProperty("diskUsage", user.getDiskUsage());
		if (user.getEmail() != null) node.setProperty("email", user.getEmail());
		node.setProperty("followersCount", user.getFollowersCount());
		node.setProperty("followingCount", user.getFollowingCount());
		if (user.getFullname() != null) node.setProperty("fullname", user.getFullname());
		if (user.getGravatarId() != null) node.setProperty("gravatarId", user.getGravatarId());
		if (user.getId() != null) node.setProperty("gitHubId", user.getId()); // note name change
		if (user.getLocation() != null) node.setProperty("location", user.getLocation());
		if (user.getLogin() != null) node.setProperty("login", user.getLogin());
		if (user.getName() != null) node.setProperty("name", user.getName());
		node.setProperty("ownedPrivateRepoCount", user.getOwnedPrivateRepoCount());
		// getPermission
		// getPlan
		node.setProperty("privateGistCount", user.getPrivateGistCount());
		node.setProperty("publicGistCount", user.getPublicGistCount());
		node.setProperty("publicRepoCount", user.getPublicRepoCount());
		node.setProperty("score", user.getScore());
		node.setProperty("totalPrivateRepoCount", user.getTotalPrivateRepoCount());
		if (user.getUsername() != null) node.setProperty("username", user.getUsername());
		return user;
	}
	
	/**
	 * Saves a repository to the graph database.
	 * 
	 * Repositories are keyed according to their full project name, which is a combination
	 * of the username and the project name. This allows differentiation between different
	 * forks of a project.
	 * 
	 * FIXME: this should return the node
	 * 
	 * @param repo
	 * @return the same repo that was passed in, unmodified.
	 */
	public Repository saveRepository(Repository repo) {
		Index <Vertex> repoidx = (Index <Vertex>)getOrCreateIndex("repository-idx");
		String projectFullName = repo.getUsername() + "/" + repo.getName();
		Vertex node = null;
		Iterable<Vertex> results = repoidx.get("name", projectFullName);
		for (Vertex v : results) {
			node = v;
		}
		if (node == null) {
			node = graph.addVertex(null);
			repoidx.put("fullname", projectFullName, node);
		}
		
		node.setProperty("type", "REPOSITORY");
		
		node.setProperty("fullname", projectFullName);
		node.setProperty("name", repo.getName());
		node.setProperty("actions", repo.getActions());
		if (repo.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(repo.getCreatedAt()));
		if (repo.getDescription() != null) node.setProperty("description", repo.getDescription());
		node.setProperty("followers", repo.getFollowers());
		node.setProperty("forks", repo.getForks());
		if (repo.getHomepage() != null) node.setProperty("homepage", repo.getHomepage());
		if (repo.getId() != null) node.setProperty("gitHubId", repo.getId()); // note name change
		// getLanguage
		node.setProperty("openIssues", repo.getOpenIssues());
		if (repo.getOrganization() != null) node.setProperty("organization", repo.getOrganization());
		if (repo.getOwner() != null) node.setProperty("owner", repo.getOwner());
		if (repo.getParent() != null) node.setProperty("parent", repo.getParent());
		// getPermission
		if (repo.getPushedAt() != null) node.setProperty("pushedAt", dateFormatter.format(repo.getPushedAt())); 
		node.setProperty("score", repo.getScore());
		node.setProperty("size", repo.getSize());
		if (repo.getSource() != null) node.setProperty("source", repo.getSource());
		if (repo.getType() != null) node.setProperty("repoType", repo.getType()); // note name change
		if (repo.getUrl() != null) node.setProperty("url", repo.getUrl());
		if (repo.getUsername() != null) node.setProperty("username", repo.getUsername());
		// getVisibility
		node.setProperty("watchers", repo.getWatchers());
		return repo;
	}
}
