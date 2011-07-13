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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Repository;
import com.github.api.v2.services.RepositoryService;
import com.github.api.v2.services.UserService;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

public class RepositoryMiner {
	private RepositoryService service = null;
	private Logger log;
	
	public RepositoryMiner(RepositoryService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public Repository getRepositoryInformation(String username, String reponame, IndexableGraph graph) {
		Repository repo = service.getRepository(username, reponame);
		Index<Vertex> repoidx = null;
		for (Index<? extends Element> idx : graph.getIndices()) {
			log.debug("Found index name: " + idx.getIndexName() + " class: " + idx.getIndexClass().toString());
			if (idx.getIndexName().equals("repo-idx") && Vertex.class.isAssignableFrom(idx.getIndexClass())) {
				log.debug("Found matching index in database");
				repoidx = (Index<Vertex>) idx; // ignore warning - we've checked for it
				break;
			}
		}
		if (repoidx == null) {
			repoidx = graph.createManualIndex("repo-idx", Vertex.class);
		}
		
		Vertex node = null;
		Iterable<Vertex> results = repoidx.get("name", repo.getName());
		for (Vertex v : results) {
			node = v;
		}
		if (node == null) {
			node = graph.addVertex(null);
			repoidx.put("name", repo.getName(), node);
		}
		
		node.setProperty("name", repo.getName());
		if (repo.getParent() != null) {
			node.setProperty("parent", repo.getParent());
		}
		if (repo.getSource() != null) {
			node.setProperty("source", repo.getSource());
		}
		
		log.info(repo.getName());
		log.info(repo.getParent());	
		log.info(repo.getSource());
		return repo;
	}

}
