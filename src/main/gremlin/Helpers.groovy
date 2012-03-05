import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import com.tinkerpop.blueprints.pgm.Vertex

class Helpers {
    static printSortedMap(Map inmap) {
        for (p in inmap.sort{a,b -> a.key <=> b.key}) {
            println "==>" + p.key + "=" + p.value
        }
    }

    static setDifference(Collection s1, Collection s2) {
        def diff = (s1 as Set) + s2
        def tmp = s1 as Set
        tmp.retainAll(s2)
        diff.removeAll(tmp)
        return diff;
    }

    
    static setDifferenceLeft(Collection s1, Collection s2) {
        return s1.intersect(setDifference(s1, s2))
    }
    
    static Date timestampToDate(String s) {
        return Date.parse(Defaults.DATE_FORMAT, s)
    }
    
    static Date timestampToDate(int i) {
        return new java.util.Date(i*1000L)
    }

    static Date timestampToDate(long l) {
        return new java.util.Date(l)
    }

    static int parseDate(long l) {
        return l/1000
    }

    static int parseDate(int i) {
        return i
    }

    static int parseDate(String s) {
        println("bbbbbbb")
        return Date.parse(Defaults.DATE_FORMAT, s).getTime()/1000
    }

    static void updateDate(Vertex v, String s) {
        if (v.getProperty(s) != null) {
            v.setProperty(s, parseDate(v.getProperty(s)))
        }
    }

    static int dateDifference(Date d1, Date d2) {
        return (int)((d1.getTime() - d2.getTime())/1000L)
    }
    
    static Collection getAllRepositoryUsers(repo) {
        def watchers = repo.in(EdgeType.REPOWATCHED).toList()
        // collaborators: have admin rights on projects
        def collaborators = repo.out(EdgeType.REPOCOLLABORATOR).toList()
        // contributors: have committed code to project
        def contributors = repo.out(EdgeType.REPOCONTRIBUTOR).toList() + \
                       repo.in(EdgeType.REPOOWNER).dedup().toList()
        def issueOwners = repo.out(EdgeType.ISSUE). \
                           in(EdgeType.ISSUEOWNER). \
                           dedup().toList()
        def issueCommenters = repo.out(EdgeType.ISSUE). \
                               out(EdgeType.ISSUECOMMENT). \
                               in(EdgeType.ISSUECOMMENTOWNER).dedup().toList()
        def pullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                 in(EdgeType.PULLREQUESTOWNER).dedup().toList()
        def openPullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                     filter{it.closedAt==null}.in(EdgeType.PULLREQUESTOWNER).dedup().toList()
        def closedPullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                       filter{it.closedAt!=null}.in(EdgeType.PULLREQUESTOWNER).dedup().toList()
        def mergedPullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                       filter{it.merged_at != null}.in(EdgeType.PULLREQUESTOWNER).dedup().toList()
        def pullRequestCommenters = repo.out(EdgeType.PULLREQUEST). \
                                     out(EdgeType.PULLREQUESTDISCUSSION). \
                                     filter{it.type==VertexType.USER.toString()}.dedup().toList()
        def mergers = repo.out(EdgeType.ISSUE). \
                       out(EdgeType.ISSUEEVENT). \
                       filter{it.event=="merged"}.in(EdgeType.ISSUEEVENTACTOR).dedup()
        def forkOwners = repo.out(EdgeType.REPOFORK). \
                          in(EdgeType.REPOOWNER).dedup().toList()
    
        // FIXME: this should be converted to constants
        def committers = repo.in("REPOSITORY").out("AUTHOR").filter{it.type=="GIT_USER"}.out("EMAIL").dedup().in("EMAIL").filter{it.type=="USER"}
         
        def allActive = (collaborators + contributors + issueOwners + \
                    issueCommenters + pullRequestOwners + \
                    openPullRequestOwners + closedPullRequestOwners + \
                    pullRequestCommenters + mergedPullRequestOwners + \
                    mergers + forkOwners).unique()
        def allUsers = (allActive as Set) + watchers
        return allUsers
    }
}
