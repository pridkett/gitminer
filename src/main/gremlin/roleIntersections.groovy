import java.util.regex.Matcher
import java.util.regex.Pattern

def loadRoleData(path) {
    f = new File(path)
    d = [:]
    f.eachLine { s ->
        //try {
            // match and extract
            matcher = ( s =~ /^([a-zA-Z0-9\/\-_\.]+):([a-zA-Z0-9\-_\.]+) = \[(.*)\]/ )
            // index 0 should be the only match
            reponame = matcher[0][1]
            role = matcher[0][2]
            users = matcher[0][3].split(", ").toList().toSet()
            // if repo has not yet been visited, init map
            if (d[role] == null) { d[role] = [:] }
            // place the entry we have just read in the map of maps
            d[role][reponame] = users
        //} catch(e) {
            //println "line not matched: " + s
        //}
    }
    return d
}

def intersections(ps) {
    ps.collect { p1 -> ps.collect { p2 -> p1.intersect(p2) }}
}

def intersectionCounts(ps) {
    ps.collect { p1 -> ps.collect { p2 -> p1.intersect(p2).size() }}
}

def intersectionCountsAsCSV(names, ps) {
    counts = intersectionCounts(ps)
    s = "," + names.join(", ") + "\n"
    (0..counts.size()-1).each{ i ->
        s += names[i] + ", " + counts[i].join(", ") + "\n"
    }
    return s
}

roleFilePath = "rails.db.20120505.repositoryUsers.txt"
roleData = loadRoleData(roleFilePath)

//roles = roleData.keySet().toList()
roles = ["contributors", "issueowners", "issuecommenters", "issueclosers", "issuesubscrbers", "allissueusers", "pullrequestowners", "mergedpullrequestowners", "pullrequestmergers", "allpullrequestusers", "forkowners", "committers", "allactiveusers", "watchers", "allusers"].sort()
projects = roleData[roles[0]].keySet().toList().sort()

roles.each { role ->
    f = new File(role + ".csv")
    ps = projects.collect { project ->
        roleData[role][project]
    }
    s = intersectionCountsAsCSV( projects, ps )
    f.write(s)
}
