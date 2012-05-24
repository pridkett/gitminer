/**
 * Builds a profile of a single developer
 * 
 * Output columns are as follows:
 * 
 * id: Int - the database id of the user
 * role: String - the defined role for the user
 * login: String - the login name
 * name: String - the users full name
 * createdAt: Int - date user created in seconds since start of epoch 
 * followers: Int - number of people following this user
 * following: Int - number of people this user is following
 * public_gist_count: Int - number of public gists
 * public_repo_count: Int - number of public repos
 * watched: Int - number of repositories watched
 * organizations: Int - number of organizations this user is a member of
 *
 * @author Patrick Wagstrom <patrick@wagstrom.net>
 */

import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols

copyProperties = ["login", "name", "createdAt", "followers", "following",
                  "public_gist_count", "public_repo_count"]

outputFields = ["id", "role"] + copyProperties +
               ["watched", "organizations"]

def readUserNames(String filename) {
    userMap = [:].withDefault{[]}
    pattern = ~/^([a-z]+) ?= ?\[(.*)\]/
    lines = new FileInputStream(filename).readLines()
    for (line in lines) {
        matcher = pattern.matcher(line)
        userMap[matcher[0][1]] = matcher[0][2].split(", ").collect{it[1..-2]}
    }
    return userMap
}

def buildDeveloperProfile(String login, String role, Graph g) {
    profileMap = [:].withDefault{null}
    user = g.idx(IndexNames.USER).get(IdCols.USER, login).next()
    
    profileMap["id"] = user.id
    profileMap["role"] = role
    copyProperties.each{profileMap[it] = user.getProperty(it)}
    if (profileMap["name"] != null) {
        profileMap["name"] = profileMap["name"].replace(",", " ")
    }
    profileMap["watched"] = user.out(EdgeType.REPOWATCHED).dedup().count()
    profileMap["organizations"] = user.out(EdgeType.ORGANIZATIONMEMBER).dedup().count()
    
    println(outputFields.collect{profileMap[it]}.join(", "))
}

m = ["READ_ONLY": true]
g = new Neo4jGraph("rails.db.20120505", m)
users = readUserNames("rails.data/rails.db.20120505.people.txt")
profiledUsers = new HashSet()
println(outputFields.join(", "))
for (role in users.keySet()) {
    for (login in users[role]) {
        if (!profiledUsers.contains(login)) {
            buildDeveloperProfile(login, role, g)
            profiledUsers.add(login)
        }
    }
}
g.shutdown()