/**
 * Simple class with static helper functions needed for Rails analysis
 * 
 * @author pwagstro
 */
class RailsHelpers {
    static readUserNames(String filename) {
        def userMap = [:].withDefault{[]}
        def pattern = ~/^([a-z]+) ?= ?\[(.*)\]/
        def lines = new FileInputStream(filename).readLines()
        for (line in lines) {
            def matcher = pattern.matcher(line)
            userMap[matcher[0][1]] = matcher[0][2].split(", ").collect{it[1..-2]}
        }
        return userMap
    }
    
   static readProjectNames(String filename) {
       return new FileInputStream(filename).readLines()
   }
}