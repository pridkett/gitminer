/**
 * rolesComparator.groovy
 *
 * this is a companion script to roles.groovy
 *
 * roles.groovy only checks for direct equivalency between roles in different
 * projects, rolesComparator assumes that there is a hierarchy of development
 * roles and therefore needs to do a different sort of a Comparison
*/

import java.util.regex.Matcher
import java.util.regex.Pattern

class GLOBALS {
    static PROJECTS_FILE = [System.getenv("HOME"), "Google Drive", "Ecosystem Research", "Data", "rails.db.20120505.roles.txt"].join(File.separator)
    static OUTPUT_PATH = [System.getenv("HOME"), "Google Drive", "Ecosystem Research", "Data"].join(File.separator)
    // static HIERARCHY = ["Collaborators", "External Collaborators", "Wannabes", "Independent", "Issues", "Lurkers"]
    static HIERARCHY = ["Lurkers", "Issues", "Independent", "Wannabes", "External Collaborators", "Collaborators"]
    static REVERSE_HIERARCHY = HIERARCHY.reverse()
    static ROLES = HIERARCHY + ["Project Rock Stars", "Project Stewards", "Prodders", "Code Warriors", "Nomads"]
}

Map loadRoleData(String path) {
    f = new File(path)
    data = [:].withDefault{[:].withDefault{[]}}
    nextLineTitle = false
    currentProject = null
    currentRole = null
    f.eachLine { it ->
        s = it.trim()
        if (nextLineTitle) {
            nextLineTitle = false;
            currentProject = s.trim()
            println("New current project: $currentProject")
        } else if (s.startsWith("******************************************")) {
            nextLineTitle = true
        } else if (s.trim().startsWith("[") && s.trim().endsWith("]")
                   && currentRole != null) {
            users = s[1..-2].split(",").collect{it.trim()}.toList().toSet()
            data[currentProject][currentRole] = users
            currentRole = null
        } else if ((role = GLOBALS.ROLES.find{s.startsWith(it)}) != null) {
            currentRole = role
        } else if (s.trim() != ""){
            println("unknown line: $s")
        }
    }
    return data;
}

String intersectionCountsAsCSV(String metric, String targetMetric, Map data) {
    projects = data.keySet()
    metrics = data[data.keySet().toList()[0]].keySet().toList()
    s = "name," + projects.join(", ") + "\n"
    projects.each{proj1 ->
        s = s + proj1
        projects.each{proj2 ->
            s = s + "," + data[proj1][metric].intersect(data[proj2][targetMetric]).size()
        }
        s = s + "\n"
    }
    return s
}

Map compareData(String outputPath, Map data) {

    
    projects = data.keySet()
    metrics = data[data.keySet().toList()[0]].keySet().toList()

    // go through and build the hierarchy set for each project
    projects.each{proj ->
        GLOBALS.HIERARCHY.eachWithIndex{role, idx ->
            data[proj][role+"-hierarchy"] =
                GLOBALS.HIERARCHY[idx..GLOBALS.HIERARCHY.size()-1].collect{data[proj][it]}.flatten().toSet()
        }
        GLOBALS.REVERSE_HIERARCHY.eachWithIndex{role, idx ->
            data[proj][role+"-reverse-hierarchy"] =
                GLOBALS.REVERSE_HIERARCHY[idx..GLOBALS.REVERSE_HIERARCHY.size()-1].collect{data[proj][it]}.flatten().toSet()
        }

    }
    metrics.each{metric ->
        println("working on $metric")
        outfile = new File(outputPath + File.separator + metric + ".csv")
        s = intersectionCountsAsCSV(metric, metric, data)
        outfile.write(s)
    }
    
    GLOBALS.HIERARCHY.each{metric ->
        println("hierarchy on $metric")
        outfile = new File(outputPath + File.separator + metric + "-hierarchy.csv")
        s = intersectionCountsAsCSV(metric, metric+"-hierarchy", data)
        outfile.write(s)
    }
    
    GLOBALS.REVERSE_HIERARCHY.each{metric ->
        println("reverse hierarchy on $metric")
        outfile = new File(outputPath + File.separator + metric + "-reverse-hierarchy.csv")
        s = intersectionCountsAsCSV(metric, metric+"-reverse-hierarchy", data)
        outfile.write(s)
    }

    return data
}
data = compareData(GLOBALS.OUTPUT_PATH, loadRoleData(GLOBALS.PROJECTS_FILE))
