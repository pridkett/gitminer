/**
 * GravatarLink.groovy
 *
 * this file contains the code that I used to update the database
 * and link up users to gravatars and vice versa
 */

import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols
import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.PropertyName

def getOrCreateGravatar(Graph g, String gravatarhash) {
    def gravataridx = g.idx(IndexNames.GRAVATAR)
    def typeidx = g.idx(IndexNames.TYPE)
    def vtx = null;
    try {
        vtx = gravataridx.get(IdCols.GRAVATAR, gravatarhash).next();
    } catch (NoSuchElementException e) {
        vtx = g.addVertex();
        vtx.setProperty(PropertyName.TYPE, VertexType.GRAVATAR);
        vtx.setProperty(PropertyName.GRAVATAR_ID, gravatarhash);
        vtx.setProperty(PropertyName.SYS_CREATED_AT, new java.util.Date().getTime()/1000 as int);
        gravataridx.put(IdCols.GRAVATAR, gravatarhash, vtx);
        typeidx.put(IdCols.TYPE, VertexType.GRAVATAR, vtx);
    }
    return vtx
}

def createEdgeIfNotExist(Graph g, Vertex source, Vertex target, String label) {
    try {
        return source.outE(label).inV.filter{it == target}.next();
    } catch (NoSuchElementException e) {
        def edge = g.addEdge(source, target, label);
        edge.setProperty(PropertyName.SYS_CREATED_AT, new java.util.Date().getTime()/1000 as int);
        return edge;
    }
}

g = new Neo4jGraph("graph.20120210.db")
g.setMaxBufferSize(10000)
// step 0: check for presence of index
gravataridx = g.idx(IndexNames.GRAVATAR)
if (gravataridx == null) {
    gravataridx = g.createManualIndex(IndexNames.GRAVATAR, Vertex.class)
}

// step 1: iterate over all of the github accounts
g.idx(IndexNames.TYPE).get("type", VertexType.USER)._(). \
  hasNot(PropertyName.GRAVATAR_ID, null). \
  sideEffect{createEdgeIfNotExist(g, it, getOrCreateGravatar(g, Helpers.gravatarIdExtract(it.getProperty(PropertyName.GRAVATAR_ID))), EdgeType.GRAVATAR); println "User: " + it}.iterate()

// setp 2: iterate over all the email accounts
g.idx(IndexNames.TYPE).get(IdCols.TYPE, VertexType.EMAIL)._(). \
  hasNot(PropertyName.EMAIL, null). \
  sideEffect{createEdgeIfNotExist(g, it, getOrCreateGravatar(g, Helpers.gravatarHash(it.getProperty(PropertyName.EMAIL))), EdgeType.GRAVATARHASH); println "Email: " + it}.iterate()
  
    
g.shutdown()