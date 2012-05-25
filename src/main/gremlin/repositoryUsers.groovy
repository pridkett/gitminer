/**
 * connects to a the database and gets all of the users for a project
 *
 * this is more advanced than Helpers.getAllRepositoryUsers as it also provides
 * a list of the users by their types, for example, active on issues, watchers,
 * committers, etc.
 * 
 * @author Patrick Wagstrom <patrick@wagstrom.net>
 */

import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols

def dumpRepositoryUsers(String reponame, Graph g) {
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).next()
    allUsers = new HashSet()
    issueUsers = new HashSet()
    pullRequestUsers = new HashSet()
    
    contributors = Helpers.getRepositoryContributors(repo).login.toList()
    println reponame + ":contributors = " + contributors
    
    issueOwners = Helpers.getRepositoryIssueOwners(repo).login.toList()
    println reponame + ":issueowners = " + issueOwners
    
    issueCommenters = Helpers.getRepositoryIssueCommenters(repo).login.toList()
    println reponame + ":issuecommenters = " + issueCommenters
    
    issueClosers = Helpers.getRepositoryIssueClosers(repo).login.toList()
    println reponame + ":issueclosers = " + issueClosers

    issueSubscribers = Helpers.getRepositoryIssueSubscribers(repo).login.toList()
    println reponame + ":issuesubscrbers = " + issueSubscribers
    
    issueUsers.addAll(issueOwners)
    issueUsers.addAll(issueCommenters)
    issueUsers.addAll(issueClosers)
    issueUsers.addAll(issueSubscribers)
    println reponame + ":allissueusers = " + issueUsers
    
    pullRequestOwners = Helpers.getRepositoryPullRequestOwners(repo).login.toList()
    println reponame + ":pullrequestowners = " + pullRequestOwners
    
    mergedPullRequestOwners = Helpers.getRepositoryMergedPullRequestOwners(repo).login.toList()
    println reponame + ":mergedpullrequestowners = " + mergedPullRequestOwners
    
    pullRequestMergers = Helpers.getRepositoryPullRequestMergers(repo).login.toList()
    println reponame + ":pullrequestmergers = " + pullRequestMergers
    
    pullRequestUsers.addAll(pullRequestOwners)
    pullRequestUsers.addAll(mergedPullRequestOwners)
    pullRequestUsers.addAll(pullRequestMergers)
    println reponame + ":allpullrequestusers = " + pullRequestUsers
    
    forkOwners = Helpers.getRepositoryForkOwners(repo).login.toList()
    println reponame + ":forkowners = " + forkOwners
    
    committers = Helpers.getRepositoryCommitters(repo).login.toList()
    println reponame + ":committers = " + committers
    
    allUsers.addAll(issueUsers)
    allUsers.addAll(pullRequestUsers)
    allUsers.addAll(contributors)
    allUsers.addAll(forkOwners)
    allUsers.addAll(committers)
    println reponame + ":allactiveusers = " + allUsers
    
    watchers = Helpers.getRepositoryWatchers(repo).login.toList()
    println reponame + ":watchers" + watchers
    
    allUsers.addAll(watchers)
    println reponame + ":allusers" + allUsers
}

m = ["READ_ONLY": "true"]
g = new Neo4jGraph("rails.db.20120505", m)
repos = RailsHelpers.readProjectNames("rails.data/rails.db.20120505.projects.txt")
repos.each{dumpRepositoryUsers(it, g)}
g.shutdown()