/**
 * Builds a profile of a single project
 * 
 * Output columns are as follows:
 * 
 * reponame: String - name of the repository (e.g. rails/rails)
 * gitHubId: Int - github's internal id
 * openIssues: Int - number of open issues at time of data pull
 * hasIssues: Boolean - whether or not issues are available
 * createdAt: Int - date repo created (seconds since epoch)
 * watchers: Int - number of people watching
 * forks: Int - number of forks
 * hasDownloads: Boolean - whether ot not the project has downloads
 * language: String - primary programming language (e.g. Ruby)
 * hasWiki: Boolean - is there a wiki for this project
 * size: Int - not certain, reported by github
 * issues: Int - total number of issues
 * issueOwners: Int - total number of distinct people who have filed issues
 * pullRequests: Int - total number of pull requests
 * pullRequestOwners: Int - total number of distinct people who have created pull requests
 * commits: Int - total number of commits in the repository
 * issueComments: Int - total number of comments on issues
 * issueCommenters: Int - total number of distinct people who have commented on issues
 * issueClosers: Int - total number of distinct people who have closed an issue
 * issueSubscribers: Int - total number of distinct people who have subscribed to at least one issue
 * forkOwners: Int - the total number of people who have forked the repository
 * 
 * @author Patrick Wagstrom <patrick@wagstrom.net>
 */

import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols

copyProperties = ["reponame", "gitHubId", "openIssues", "hasIssues",
                  "createdAt", "watchers", "forks", "hasDownloads", "language",
                  "hasWiki", "size"]

outputFields = ["id"] + 
               copyProperties + ["contributors", "collaborators", 
                                 "issues", "issueOwners", "pullRequests",
                                 "pullRequestOwners", "commits",
                                 "issueComments", "issueCommenters",
                                 "issueClosers", "issueSubscribers",
                                 "pullRequestCommenters", "pullRequestMergers",
                                 "forkOwners"]

def readProjectNames(String filename) {
    return new FileInputStream(filename).readLines()
}

def buildProjectProfile(String reponame, Graph g) {
    profileMap = [:].withDefault{null}
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, reponame).next()

    profileMap["id"] = repo.id
    copyProperties.each{profileMap[it] = repo.getProperty(it)}

    profileMap["contributors"] = Helpers.getRepositoryContributors(repo).count()
    profileMap["collaborators"] = Helpers.getRepositoryCollaborators(repo).count()
    profileMap["issues"] = repo.out(EdgeType.ISSUE).count()
    profileMap["issueOwners"] = Helpers.getRepositoryIssueOwners(repo).count()
    profileMap["pullRequests"] = repo.out(EdgeType.PULLREQUEST).count()
    profileMap["pullRequestOwners"] = Helpers.getRepositoryPullRequestOwners(repo).count()
    profileMap["commits"] = repo.in(EdgeType.REPOSITORY).has("type", VertexType.COMMIT).count()
    profileMap["issueComments"] = repo.out(EdgeType.ISSUE).out(EdgeType.ISSUECOMMENT).dedup().count()
    profileMap["issueCommenters"] = Helpers.getRepositoryIssueCommenters(repo).count()
    profileMap["issueClosers"] = Helpers.getRepositoryIssueClosers(repo).count()
    profileMap["issueSubscribers"] = Helpers.getRepositoryIssueSubscribers(repo).count()
    profileMap["pullRequestCommenters"] = Helpers.getRepositoryPullRequestCommenters(repo).count()
    profileMap["pullRequestMergers"] = Helpers.getRepositoryPullRequestMergers(repo).count()
    profileMap["forkOwners"] = Helpers.getRepositoryForkOwners(repo).count()

    println(outputFields.collect{profileMap[it]}.join(", "))
}

m = ["READ_ONLY": true]
g = new Neo4jGraph("rails.db.20120505", m)
projects = readProjectNames("rails.data/rails.db.20120505.projects.txt")
println(outputFields.join(", "))
for (project in projects) {
    buildProjectProfile(project, g)
}
g.shutdown()