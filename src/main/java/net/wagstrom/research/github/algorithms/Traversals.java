package net.wagstrom.research.github.algorithms;

import java.util.HashSet;
import java.util.Set;

import net.wagstrom.research.github.BlueprintsDriver;
import net.wagstrom.research.github.EdgeType;
import net.wagstrom.research.github.PropertyName;
import net.wagstrom.research.github.VertexType;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class Traversals extends Algorithm {
    public Traversals(BlueprintsDriver driver) {
        super(driver);
    }
    
    /**
     * Get all the users related to a given repository
     *
     * There are six ways that related users are found:
     *
     * Users watching the repository
     * Users collaborating with the repository
     * Users filing issues with the repository
     * Users commenting on issues with the repository
     * Users creating pull requests with the repository
     * Users creating discussions on pull requests in the repository
     *
     * This DOES NOT capture the following relationships:
     * 
     * Users who have forked the repository
     *
     * Without doing an iteration over the objects we need to do an
     * untyped cast using type erasure. Yeah, it sucks.
     *
     * @param repo The vertex to traverse from
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<Vertex> getAllRepositoryUsers(final Vertex repo) {
        Set<Object> users = new HashSet<Object>();
        GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
        
        users.addAll(pipe.start(repo).in(EdgeType.REPOWATCHED).toList());
        users.addAll(pipe.start(repo).in(EdgeType.REPOCOLLABORATOR).toList());
        users.addAll(pipe.start(repo).out(EdgeType.ISSUE).in(EdgeType.ISSUEOWNER).dedup().toList());
        users.addAll(pipe.start(repo).out(EdgeType.ISSUE).out(EdgeType.ISSUECOMMENT).in(EdgeType.ISSUECOMMENTOWNER).dedup().toList());
        users.addAll(pipe.start(repo).out(EdgeType.PULLREQUEST).in(EdgeType.PULLREQUESTOWNER).dedup().toList());
        users.addAll(pipe.start(repo).out(EdgeType.PULLREQUEST).
             out(EdgeType.PULLREQUESTDISCUSSION).in().
             has(PropertyName.TYPE, VertexType.USER).dedup().toList());
        return (Set<Vertex>)(Set<?>)users;
    }

    /**
     * Get the names of the repositories related to the current repository.
     *
     * There are multiple different ways to get all of the related
     * repositories:
     *
     * The watched links of all of the users related to the project
     * Events connected to users related to the project
     *
     * Without doing an iteration over the objects we need to do an
     * untyped cast using type erasure. Yeah, it sucks.
     * 
     * @param reponame
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<String> getAllChildRepositories(final String reponame) {
        Vertex repo = driver.getOrCreateRepository(reponame);
        Set<Object> childRepositories = new HashSet<Object>();
        
        Set<Vertex> users = getAllRepositoryUsers(repo);
        GremlinPipeline<Vertex, String> pipe = new GremlinPipeline<Vertex, String>();
        
        
        pipe.setStarts(users);
        pipe.out(EdgeType.USEREVENT).out().
             has(PropertyName.TYPE, VertexType.REPOSITORY).
             hasNot(PropertyName.FULLNAME, "/").dedup().
             property(PropertyName.FULLNAME).
             fill(childRepositories);
        
        
        pipe.out(EdgeType.REPOWATCHED).dedup().
             property(PropertyName.FULLNAME).fill(childRepositories);
        
        return (Set<String>)(Set<?>)childRepositories;
    }

}
