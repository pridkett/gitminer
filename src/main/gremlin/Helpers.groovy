import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.PropertyName
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.Element
import java.security.MessageDigest

class Helpers {
    static printSortedMap(Map inmap) {
        for (p in inmap.sort{a,b -> a.key <=> b.key}) {
            println "==>" + p.key + "=" + p.value
        }
    }

    static setDifference(Collection s1, Collection s2) {
        def set1 = s1.clone().toSet()
        set1.removeAll(s1.toSet().intersect(s2.toSet()))
        return set1
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
        return Date.parse(Defaults.DATE_FORMAT, s).getTime()/1000
    }

    static void updateDate(Element v, String s) {
        if (v.getProperty(s) != null) {
            v.setProperty(s, parseDate(v.getProperty(s)))
        }
    }

    static int dateDifference(Date d1, Date d2) {
        return (int)((d1.getTime() - d2.getTime())/1000L)
    }
    
    static int dateDifferenceAbs(Date d1, Date d2) { 
        return Math.abs(dateDifference(d1, d2))
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
                       filter{it.event=="merged"}.in(EdgeType.ISSUEEVENTACTOR).dedup().toList()
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
    
    static String gravatarHash(String email) {
        def m = MessageDigest.getInstance("MD5")
        m.update(email.trim().toLowerCase().getBytes())
        def l = new BigInteger(1, m.digest())
        return l.toString(16)
    }
    
    /**
     * gravatarId's can be problematic as they can be either of:
     * https://secure.gravatar.com/avatar/ee85853909657f47c8a68e8a9bc7d992?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png
     * e3e98bfa99e82ac8b0cb63660dc23b14
     * 
     * This function extracts the proper value
     */
    static String gravatarIdExtract(String gravatarId) {
        try {
            return (gravatarId =~ /([a-f0-9]{32})/)[0][0]
        } catch (e) {
            return null;
        }
    }
    
    
    static getAllGitAccounts(g, Vertex user) {
        // getting all of a users git accounts is tricky because they don't make all of their email addresses
        // public. Luckily, using these two methods we do a pretty good job of getting all of a users git_user
        // accounts
        def gitAccounts = user.out(EdgeType.EMAIL). \
                           in(EdgeType.EMAIL). \
                           has("type", VertexType.GIT_USER). \
                           dedup().toSet()
    
        // this code has been superseded as it isn't always that accurate and can
        // grab accounts that don't belong to this user
        // gitAccounts = (gitAccounts as Set) + user.out(EdgeType.ISSUEEVENTACTOR). \
        //                  in(EdgeType.ISSUEEVENT).in(EdgeType.ISSUE). \
        //                  filter{it == repo}.back(3).out(EdgeType.EVENTCOMMIT). \
        //                  out(EdgeType.COMMITTER).dedup().toList()
        
        // here we need to be a little careful with finding additional accounts
        // this pipe takes all of the commits this person has tied to an issue,
        // and filters for those email addresses which are not associated with
        // a user yet. It assumes, and this is a big assumption, that if one of
        // these unparented links shows up in both COMMITTERS and PARENTS then
        // the user probably owns that account
        // traceAccountsCommitter = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT).out(EdgeType.COMMITTER). \
        //                          filter{it.type=="GIT_USER"}.out("EMAIL").dedup().filter{it.in("EMAIL").filter{it.type == "USER"}.count() == 0}.back(4).toList()
        // traceAccountsAuthor = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT).out(EdgeType.COMMITAUTHOR). \
        //                          filter{it.type=="GIT_USER"}.out("EMAIL").dedup().filter{it.in("EMAIL").filter{it.type == "USER"}.count() == 0}.back(4).toList()
        // traceAccounts = (traceAccountsCommitter as Set) + traceAccountsAuthor
        // a slightly more complicated but more accurate version of the above commands
        // this version requires that the supposedly unattached commit have the same author
        // and committer.
        def traceAccounts = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT). \
             filter{it.out(EdgeType.COMMITTER).filter{it.type==VertexType.GIT_USER}.out(EdgeType.EMAIL).next() == \
                    it.out(EdgeType.COMMITAUTHOR).filter{it.type==VertexType.GIT_USER}.out(EdgeType.EMAIL).next()}. \
             out(EdgeType.COMMITTER). \
             filter{it.type==VertexType.GIT_USER}.out(EdgeType.EMAIL).dedup().filter{it.in(EdgeType.EMAIL).filter{it.type == VertexType.USER}.count() == 0}. \
             back(4).dedup().toSet()
        
        def gravatars = user.out(EdgeType.GRAVATAR). \
                         in(EdgeType.GRAVATARHASH). \
                         has(PropertyName.TYPE, VertexType.EMAIL). \
                         in(VertexType.EMAIL). \
                         has(PropertyName.TYPE, VertexType.GIT_USER).toSet()
                
        gitAccounts = gitAccounts + traceAccounts + gravatars
         
        def allGitAccounts = [] as Set
        for (email in gitAccounts._().out(EdgeType.EMAIL).email.dedup().toSet()) {
            allGitAccounts += g.idx(IndexNames.EMAIL).get(IdCols.EMAIL, email)._().in(EdgeType.EMAIL).has(PropertyName.TYPE, VertexType.GIT_USER).toSet()
        }
    
        return allGitAccounts
    }
}
